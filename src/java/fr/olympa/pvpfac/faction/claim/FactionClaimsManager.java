package fr.olympa.pvpfac.faction.claim;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.sql.OlympaStatement.StatementType;
import fr.olympa.pvpfac.faction.Faction;

public class FactionClaimsManager implements Listener {

	private static final String tableName = "`pvpfac_claims`";

	private static final OlympaStatement createClaim = new OlympaStatement(StatementType.INSERT, tableName, "x", "z", "faction_id", "type", "world_id");
	private static final OlympaStatement selectClaimByChunk = new OlympaStatement(StatementType.SELECT, tableName, new String[] { "x", "z" }, new String[] {});
	private static final OlympaStatement selectClaimByFaction = new OlympaStatement(StatementType.SELECT, tableName, new String[] { "faction_id" }, new String[] {});
	private static final OlympaStatement updateClaim = new OlympaStatement(StatementType.UPDATE, tableName, new String[] { "faction_id", "type", "owner_ids" }, "id");

	private Cache<Chunk, FactionClaim> claims = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();

	public FactionClaimsManager() throws SQLException {
		//		OlympaCore.getInstance().getDatabase().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
		//				"  `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT," +
		//				"  `region` VARBINARY(8000) NOT NULL," +
		//				"  `clan` INT NULL DEFAULT -1," +
		//				"  `sign` VARCHAR(100) NOT NULL," +
		//				"  `spawn` VARCHAR(100) NOT NULL," +
		//				"  `price` INT NOT NULL," +
		//				"  `next_payment` BIGINT NOT NULL DEFAULT -1," +
		//				"  PRIMARY KEY (`id`))");
	}

	public FactionClaim insertClaim(FactionClaim claim) throws SQLException {
		PreparedStatement statement = createClaim.getStatement();
		int i = 1;
		statement.setLong(i++, claim.getX());
		statement.setLong(i++, claim.getZ());
		Faction faction = claim.getFaction();
		if (faction == null)
			statement.setObject(i++, null);
		else
			statement.setInt(i++, claim.getFaction().getID());
		statement.setInt(i++, claim.getType().ordinal());
		statement.setInt(i++, Bukkit.getWorlds().indexOf(claim.getWorld()));
		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();

		claim.setId(resultSet.getInt(1));
		resultSet.close();
		return claim;
	}

	public FactionClaim updateClaim(FactionClaim factionClaim) throws SQLException {
		PreparedStatement statement = updateClaim.getStatement();
		int i = 1;
		statement.setLong(i++, factionClaim.getId());
		Faction faction = factionClaim.getFaction();
		if (faction == null)
			statement.setObject(i++, null);
		else
			statement.setInt(i++, factionClaim.getFaction().getID());
		statement.setInt(i++, factionClaim.getType().ordinal());
		Set<Integer> ownerIds = factionClaim.getOwnerIds();
		statement.setString(i++, ownerIds == null || ownerIds.isEmpty() ? null : ownerIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
		statement.executeUpdate();
		statement.close();
		return factionClaim;
	}

	private FactionClaim selectClaim(Chunk chunk) throws SQLException {
		PreparedStatement statement = selectClaimByChunk.getStatement();
		statement.setInt(1, chunk.getX());
		statement.setInt(2, chunk.getZ());
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			return getFactionClaim(resultSet);
		return null;
	}

	private Set<FactionClaim> selectClaims(Faction faction) throws SQLException {
		Set<FactionClaim> claims = new HashSet<>();
		PreparedStatement statement = selectClaimByFaction.getStatement();
		statement.setInt(1, faction.getID());
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
			claims.add(getFactionClaim(resultSet));
		return claims;
	}

	private FactionClaim getFactionClaim(ResultSet resultSet) throws SQLException {
		return new FactionClaim(
				resultSet.getInt("id"),
				resultSet.getInt("world_id"),
				resultSet.getInt("x"),
				resultSet.getInt("z"),
				resultSet.getInt("faction_id"),
				resultSet.getInt("type"),
				resultSet.getString("owner_ids"));
	}

	public void addClaim(FactionClaim factionClaim) throws SQLException {
		claims.put(factionClaim.getChunk(), factionClaim);
		updateClaim(factionClaim);
	}

	public void forceClaim(Chunk chunk, FactionClaimType type) throws SQLException {
		FactionClaim claim = new FactionClaim(chunk, type);
		claims.put(chunk, claim);
		insertClaim(claim);
	}

	public Set<FactionClaim> getChunksAround(Chunk chunk) {
		World world = chunk.getWorld();
		Set<FactionClaim> claims = new HashSet<>();
		claims.add(getByChunk(world.getChunkAt(chunk.getX() + 1, chunk.getX() + 1)));
		claims.add(getByChunk(world.getChunkAt(chunk.getX() + 1, chunk.getZ())));
		claims.add(getByChunk(world.getChunkAt(chunk.getX(), chunk.getZ() + 1)));
		claims.add(getByChunk(world.getChunkAt(chunk.getX() - 1, chunk.getX() - 1)));
		return claims;
	}

	// TODO Optimize
	public Set<FactionClaim> getByFaction(Faction faction) {
		try {
			return selectClaims(faction);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public FactionClaim getByChunk(Chunk chunk) {
		try {
			FactionClaim claim = claims.getIfPresent(chunk);
			if (claim == null) {
				claim = selectClaim(chunk);
				if (claim == null) {
					claim = new FactionClaim(chunk, FactionClaimType.WILDERNESS);
					insertClaim(claim);
				}
				claims.put(chunk, claim);
			}
			return claim;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		claims.invalidate(chunk);
	}

}
