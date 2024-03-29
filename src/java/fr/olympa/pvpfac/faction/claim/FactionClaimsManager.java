package fr.olympa.pvpfac.faction.claim;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.api.common.sql.statement.StatementType;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.faction.claim.FactionClaim.ClaimId;
import fr.olympa.pvpfac.world.WorldsManager;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

	private static final OlympaStatement SELECT_CLAIM_BY_CHUNK = new OlympaStatement(StatementType.SELECT, tableName, new String[]{ "x", "z" });
	private static final OlympaStatement SELECT_CLAIMS_BY_FACTION = new OlympaStatement(StatementType.SELECT, tableName, new String[]{ "faction_id" });

	private static final OlympaStatement CREATE_CLAIM = new OlympaStatement(
		"INSERT INTO " + tableName +
		" (`x`, `z`, `claim_type`, `faction_id`, `members_players`, `members_factions`)" +
		" VALUES (?, ?, ?, ?, ?, ?);")
		.returnGeneratedKeys();

	private static final OlympaStatement UPDATE_CLAIM = new OlympaStatement(
		"UPDATE " + tableName +
		" SET `claim_type` = ?, `faction_id` = ?, `members_players` = ?, `members_factions` = ? WHERE `claim_id` = ?;");

	private final Cache<Chunk, FactionClaim> claims = CacheBuilder.newBuilder().recordStats().expireAfterAccess(10, TimeUnit.MINUTES).build();

	public FactionClaimsManager() {
		try {
			OlympaCore.getInstance().getDatabase().createStatement().executeUpdate(
				"CREATE TABLE IF NOT EXISTS " + tableName + " (" +
				"  `claim_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT," +
				"  `x` INT NOT NULL," +
				"  `z` INT NOT NULL," +
				"  `claim_type` VARCHAR(10) NOT NULL," +
				"  `faction_id` INT," +
				"  `members_players` VARCHAR(400)," +
				"  `members_factions` VARCHAR(400)," +
				"  PRIMARY KEY (`claim_id`))"
			);
		} catch (final SQLException e) {
			PvPFaction.getInstance().getLogger().severe("Unable to create " + tableName + " table!!");
			e.printStackTrace();
		}
	}

	public void updateClaim(final FactionClaim claim) {
		try (final PreparedStatement statement = UPDATE_CLAIM.createStatement()) {
			int i = 1;
			statement.setString(i++, claim.getType().toString());
			statement.setObject(i++, claim.getFaction() == null ? null : claim.getFaction().getID());
			statement.setString(i++, claim.getPlayersMembersAsJson());
			statement.setString(i++, claim.getFactionMembersAsJson());

			statement.setLong(i++, claim.getClaimId().getId());

			statement.executeUpdate();
		} catch (final SQLException e) {
			e.addSuppressed(new Throwable("§cFailed to SAVE chunk " + claim.getClaimId() + "as claim of " + (claim.getFaction() == null ? "§4NONE" : claim.getFaction().getName())));
			e.printStackTrace();
		}
	}

	public @Nullable FactionClaim ofChunk(final Chunk chunk) {
		FactionClaim claim = claims.getIfPresent(chunk);
		if (claim != null) {
			return claim;
		}

		try (final PreparedStatement statement = SELECT_CLAIM_BY_CHUNK.createStatement()) {
			int i = 1;
			statement.setInt(i++, chunk.getX());
			statement.setInt(i++, chunk.getZ());
			final ResultSet resultSet = statement.executeQuery();

			if (resultSet.next())
			//System.out.println("retrieved claim " + getFactionClaim(resultSet) + " from database");
			{
				return getFactionClaim(resultSet);
			} else {
				final PreparedStatement insert = CREATE_CLAIM.createStatement();
				int j = 1;

				insert.setInt(j++, chunk.getX());
				insert.setInt(j++, chunk.getZ());
				insert.setString(j++, FactionClaimType.NORMAL.toString());
				insert.setObject(j++, null);
				insert.setObject(j++, null);
				insert.setObject(j++, null);

				insert.executeUpdate();

				final ResultSet inserted = insert.getGeneratedKeys();
				if (inserted.next()) {
					claim = new FactionClaim(new ClaimId(inserted.getInt("claim_id"), chunk), FactionClaimType.NORMAL, null, null, null);
					claims.put(chunk, claim);

					//System.out.println("created new claim : " + claim);
					return claim;

				} else {
					throw new SQLException("Impossible to create claim at " + chunk.getX() + ", " + chunk.getZ());
				}
			}

		} catch (final SQLException e) {
			PvPFaction.getInstance().getLogger().warning("Failed to LOAD claim at " + chunk.getX() + ", " + chunk.getZ());
			e.printStackTrace();
			return null;
		}
	}

	private FactionClaim getFactionClaim(final ResultSet resultSet) throws SQLException {
		final FactionClaim claim = new FactionClaim(
			new ClaimId(resultSet.getInt("claim_id"), resultSet.getInt("x"), resultSet.getInt("z")),
			FactionClaimType.fromString(resultSet.getString("claim_type")),
			resultSet.getObject("faction_id") == null ? null : resultSet.getInt("faction_id"),
			resultSet.getString("members_players"),
			resultSet.getString("members_factions")
		);

		claims.put(WorldsManager.CLAIM_WORLD.getWorld().getChunkAt(claim.getClaimId().getX(), claim.getClaimId().getZ()), claim);
		return claim;
	}

	public @Nullable Set<FactionClaim> ofFaction(final Faction faction) {
		try (final PreparedStatement statement = SELECT_CLAIMS_BY_FACTION.createStatement()) {
			statement.setInt(1, faction.getID());
			final ResultSet resultSet = statement.executeQuery();

			final Set<FactionClaim> factionClaims = new HashSet<>();
			while (resultSet.next()) {
				factionClaims.add(getFactionClaim(resultSet));
			}
			return factionClaims;

		} catch (final SQLException e) {
			PvPFaction.getInstance().getLogger().warning("Failed to LOAD claims of " + faction.getName());
			e.printStackTrace();
			return null;
		}
	}

	@EventHandler
	public void onChunkUnload(final ChunkUnloadEvent event) {
		claims.invalidate(event.getChunk());
	}

}
