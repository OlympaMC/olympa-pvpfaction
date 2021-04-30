package fr.olympa.pvpfac.faction.claim;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.sql.statement.StatementType;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.faction.claim.FactionClaim.ClaimId;
import fr.olympa.pvpfac.world.WorldsManager;

public class FactionClaimsManager implements Listener {

	private static final String tableName = "`pvpfac_claims`";

	/*private static final SQLColumn<FactionClaim> COLUMN_ID = new SQLColumn<FactionClaim>("id", "INT(20) unsigned NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(FactionClaim::getClaimId);
	private static final SQLColumn<FactionClaim> COLUMN_X = new SQLColumn<>("x", "INT(20) NULL DEFAULT NULL", Types.INTEGER);
	private static final SQLColumn<FactionClaim> COLUMN_Z = new SQLColumn<>("z", "INT(20) NULL DEFAULT NULL", Types.INTEGER);
	private static final SQLColumn<FactionClaim> COLUMN_FACTION_ID = new SQLColumn<>("faction_id", "INT(10) UNSIGNED NULL DEFAULT NULL", Types.INTEGER);
	private static final SQLColumn<FactionClaim> COLUMN_OWNER_IDS = new SQLColumn<>("owner_ids", "TINYTEXT NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR);
	private static final SQLColumn<FactionClaim> COLUMN_TYPE = new SQLColumn<>("owner_ids", "TINYINT(1) NULL DEFAULT NULL", Types.INTEGER);
	private static final SQLColumn<FactionClaim> COLUMN_WORLD_ID = new SQLColumn<>("world_id", "TINYINT(1) NULL DEFAULT NULL", Types.INTEGER);*/

	//private static final OlympaStatement createClaim = new OlympaStatement(StatementType.INSERT, tableName, "world_name", "x", "z", "faction_id", "members").returnGeneratedKeys();

	private static final OlympaStatement selectClaimByChunk = new OlympaStatement(StatementType.SELECT, tableName, new String[] { "x", "z" }, new String[] {});
	private static final OlympaStatement selectClaimsByFaction = new OlympaStatement(StatementType.SELECT, tableName, new String[] { "faction_id" }, new String[] {});

	private static final OlympaStatement createClaim = new OlympaStatement(
			"INSERT INTO " + tableName +
					" (`x`, `z`, `claim_type`, `faction_id`, `members_players`, `members_factions`)" +
					" VALUES (?, ?, ?, ?, ?, ?);")
			.returnGeneratedKeys();

	private static final OlympaStatement updateClaim = new OlympaStatement(
			"UPDATE " + tableName +
					" SET `claim_type` = ?, `faction_id` = ?, `members_players` = ?, `members_factions` = ? WHERE `claim_id` = ?;");

	private Cache<Chunk, FactionClaim> claims = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

	public FactionClaimsManager() {
		try {
			OlympaCore.getInstance().getDatabase().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
					"  `claim_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT," +
					"  `x` INT NOT NULL," +
					"  `z` INT NOT NULL," +
					"  `claim_type` VARCHAR(10) NOT NULL," +
					"  `faction_id` INT," +
					"  `members_players` VARCHAR(400)," +
					"  `members_factions` VARCHAR(400)," +
					"  PRIMARY KEY (`claim_id`))");
		} catch (SQLException e) {
			PvPFaction.getInstance().getLogger().severe("Unable to create " + tableName + " table!!");
			e.printStackTrace();
		}
	}

	public void updateClaim(FactionClaim claim) {
		try (PreparedStatement statement = updateClaim.createStatement()) {
			int i = 1;
			statement.setString(i++, claim.getType().toString());
			statement.setObject(i++, claim.getFaction() == null ? null : claim.getFaction().getID());
			statement.setString(i++, claim.getPlayersMembersAsJson());
			statement.setString(i++, claim.getFactionMembersAsJson());

			statement.setLong(i++, claim.getClaimId().getId());
			
			statement.executeUpdate();
		} catch (SQLException e) {
			e.addSuppressed(new Throwable("§cFailed to SAVE chunk " + claim.getClaimId() + "as claim of " + (claim.getFaction() == null ? "§4NONE" : claim.getFaction().getName())));
			e.printStackTrace();
		}
	}

	public FactionClaim ofChunk(Chunk chunk) {
		FactionClaim claim = claims.getIfPresent(chunk);
		if (claim != null)
			return claim;

		try (PreparedStatement statement = selectClaimByChunk.createStatement()) {
			int i = 1;
			statement.setInt(i++, chunk.getX());
			statement.setInt(i++, chunk.getZ());
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				//System.out.println("retrieved claim " + getFactionClaim(resultSet) + " from database");
				return getFactionClaim(resultSet);
			}else {
				PreparedStatement insert = createClaim.createStatement();
				int j = 1;

				insert.setInt(j++, chunk.getX());
				insert.setInt(j++, chunk.getZ());
				insert.setString(j++, FactionClaimType.NORMAL.toString());
				insert.setObject(j++, null);
				insert.setObject(j++, null);
				insert.setObject(j++, null);

				insert.executeUpdate();
				
				ResultSet inserted = insert.getGeneratedKeys();
				if (inserted.next()) {
					claim = new FactionClaim(new ClaimId(inserted.getInt("claim_id"), chunk), FactionClaimType.NORMAL, null, null, null);
					claims.put(chunk, claim);
					
					//System.out.println("created new claim : " + claim);
					return claim;

				} else
					throw new SQLException("Impossible to create claim at " + chunk.getX() + ", " + chunk.getZ());
			}

		} catch (SQLException e) {
			PvPFaction.getInstance().getLogger().warning("Failed to LOAD claim at " + chunk.getX() + ", " + chunk.getZ());
			e.printStackTrace();
			return null;
		}
	}

	public Set<FactionClaim> ofFaction(Faction faction) {
		try (PreparedStatement statement = selectClaimsByFaction.createStatement()) {
			statement.setInt(1, faction.getID());
			ResultSet resultSet = statement.executeQuery();

			Set<FactionClaim> factionClaims = new HashSet<>();
			while (resultSet.next())
				factionClaims.add(getFactionClaim(resultSet));
			return factionClaims;

		} catch (SQLException e) {
			PvPFaction.getInstance().getLogger().warning("Failed to LOAD claims of " + faction.getName());
			e.printStackTrace();
			return null;
		}
	}

	private FactionClaim getFactionClaim(ResultSet resultSet) throws SQLException {
		FactionClaim claim = new FactionClaim(
				new ClaimId(resultSet.getInt("claim_id"), resultSet.getInt("x"), resultSet.getInt("z")),
				FactionClaimType.fromString(resultSet.getString("claim_type")),
				resultSet.getObject("faction_id") == null ? null : resultSet.getInt("faction_id"),
				resultSet.getString("members_players"),
				resultSet.getString("members_factions"));

		claims.put(WorldsManager.CLAIM_WORLD.getWorld().getChunkAt(claim.getClaimId().getX(), claim.getClaimId().getZ()), claim);
		return claim;
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		claims.invalidate(event.getChunk());
	}

}
