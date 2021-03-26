package fr.olympa.pvpfac.faction.claim;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.naming.directory.InvalidAttributesException;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.region.tracking.flags.DamageFlag;
import fr.olympa.api.region.tracking.flags.PlayerBlockInteractFlag;
import fr.olympa.api.region.tracking.flags.PlayerBlocksFlag;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.sql.statement.StatementType;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.faction.claim.FactionClaim.ChunkId;
import fr.olympa.pvpfac.player.FactionPlayer;

public class FactionClaimsManager {

	private static final String claimsTableName = "`pvpfac_claims`";
	//private static final String membersPlayersTableName = "`pvpfac_claims_members_players`";
	//private static final String membersFactionsTableName = "`pvpfac_claims_members_factions`";

	//private static final OlympaStatement createClaim = new OlympaStatement(StatementType.INSERT, tableName, "world_name", "x", "z", "faction_id", "members").returnGeneratedKeys();
	private static final OlympaStatement selectClaimByChunk = new OlympaStatement(StatementType.SELECT, claimsTableName, new String[] { "x", "z" }, new String[] {});
	private static final OlympaStatement selectClaimsByFaction = new OlympaStatement(StatementType.SELECT, claimsTableName, new String[] { "faction_id" }, new String[] {});
	//private static final OlympaStatement deleteClaimOfChunk = new OlympaStatement("DELETE FROM " + claimsTableName + " WHERE `id`= ?;");

	private static final OlympaStatement createClaim = new OlympaStatement("INSERT INTO " + claimsTableName + " (`x`, `z`) VALUES (?, ?);").returnGeneratedKeys();
	private static final OlympaStatement updateClaim = new OlympaStatement("UPDATE " + claimsTableName + " SET `faction_id` = ?, `members_players` = ?, `members_factions` = ? WHERE `id` = ?;");
	
	private Set<Faction> fullyLoadedFactionsClaims = new HashSet<Faction>();
	private Cache<ChunkId, FactionClaim> claims = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES)
			.removalListener(new RemovalListener<ChunkId, FactionClaim>() {
				@Override
				public void onRemoval(RemovalNotification<ChunkId, FactionClaim> notification) {
					fullyLoadedFactionsClaims.remove(notification.getValue().getFaction());
				}
			}).build();
	

	public FactionClaimsManager() {
		try {
			OlympaCore.getInstance().getDatabase().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + claimsTableName + " (" +
					//"  `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT," +
					"  `id` INT PRIMARY KEY NOT NULL AUTO_INCREMENT," +
					"  `x` INT NOT NULL," +
					"  `z` INT NOT NULL," +
					"  `faction_id` INT," +
					"  `members_players` VARCHAR(255)," +
					"  `faction_id` VARCHAR(255));");
		} catch (SQLException e) {
			PvPFaction.getInstance().getLogger().severe("Unable to create " + claimsTableName + "table!!");
			e.printStackTrace();
		}
	}

	/*
	public FactionClaim createClaim(FactionClaim claim) throws SQLException {
		if (claim.getFaction() == null)
			throw new SQLException("Trying to create a claim with a null faction id!");
		
		try (PreparedStatement statement = createClaim.createStatement()) {
			int i = 1;
			statement.setString(i++, claim.getWorld().getName());
			statement.setLong(i++, claim.getX());
			statement.setLong(i++, claim.getZ());
			statement.setInt(i++, claim.getFaction().getID());
			statement.setString(i++, claim.getMembersToJson());
			statement.executeUpdate();
			ResultSet resultSet = statement.getGeneratedKeys();
			resultSet.next();

			claim.setId(resultSet.getInt(1));
			return claim;
		}
	}*/
/*
	private void deleteClaim(FactionClaim claim) {
		try(PreparedStatement statement = deleteClaimOfChunk.createStatement()) {
			int i = 1;
			statement.setString(i++, claim.getChunkId().getWorld().getName());
			statement.setLong(i++, claim.getChunkId().getX());
			statement.setLong(i++, claim.getChunkId().getZ());
			statement.executeUpdate();
			
			//claims.put(claim.getChunkId(), FactionClaim.WILDERNESS);
		} catch (SQLException e) {
			PvPFaction.getInstance().getLogger().warning("Failed to DELETE chunk " + claim.getChunkId().getX() + ", " + claim.getChunkId().getZ() + " as claim of " + claim.getFaction().getName());
			e.printStackTrace();
		}
	}*/

	
	/**
	 * Detete all claims of a faction. Should be used for faction disband for example.
	 * @param faction
	 
	public void deleteClaims(Faction faction) {
		fromFaction(faction).forEach(claim -> claim.setFaction(null));
		fullyLoadedFactionsClaims.remove(faction);
	}*/
	
	/**
	 * Update claim in database for any reason. Direct modification of FactionClaims execute update automatically. 
	 * <br>For removing all claims of a faction prefer usage of deleteClaims(Faction).
	 * @param claim
	 */
	public void updateClaim(FactionClaim claim) {
		try(PreparedStatement statement = updateClaim.createStatement()) {
			int i = 1;
			//statement.setString(i++, claim.getChunkId().getWorld().toString());
			//statement.setLong(i++, claim.getChunkId().getX());
			//statement.setLong(i++, claim.getChunkId().getZ());
			statement.setInt(i++, claim.getFaction().getID());
			statement.setString(i++, claim.getPlayersMembersToJson());
			statement.setString(i++, claim.getFactionMembersToJson());
			
			statement.setInt(i++, claim.getId());
			//statement.setString(i++, claim.getPlayersMembersToJson());
			//statement.setString(i++, claim.getFactionMembersToJson());
			statement.executeUpdate();
		} catch (SQLException e) {
			PvPFaction.getInstance().getLogger().warning("Failed to SAVE chunk " + claim.getId() + " as claim of " + claim.getFaction() == null ? "§4NONE" : claim.getFaction().getName());
			e.printStackTrace();
		}
	}

	/*
	public FactionClaim updateClaim(FactionClaim factionClaim) throws SQLException {
		try (PreparedStatement statement = updateClaim.createStatement()) {
			int i = 1;
			statement.setLong(i++, factionClaim.getId());
			Faction faction = factionClaim.getFaction();
			if (faction == null)
				statement.setObject(i++, null);
			else
				statement.setInt(i++, factionClaim.getFaction().getID());
			//statement.setInt(i++, factionClaim.getType().ordinal());
			//Set<Integer> ownerIds = factionClaim.getOwnerIds();
			//statement.setString(i++, ownerIds == null || ownerIds.isEmpty() ? null : ownerIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
			statement.executeUpdate();
			return factionClaim;
		}
	}*/
	
	/**
	 * Get FactionClaim for corresponding chunk.
	 * @param chunk
	 * @return
	 */
	public FactionClaim fromChunk(Chunk chunk) {
		FactionClaim claim = claims.getIfPresent(chunk);//TODO vérifier que ChunkId#equals(Chunk) fonctionne bien !!
		if (claim != null)
			return claim;
		
		try (PreparedStatement statement = selectClaimByChunk.createStatement()) {
			int i = 1;
			statement.setString(i++, chunk.getWorld().getName());
			statement.setInt(i++, chunk.getX());
			statement.setInt(i++, chunk.getZ());
			ResultSet resultSet = statement.executeQuery();
			
			if (resultSet.next())
				claim = toFactionClaim(resultSet);
			else {
				PreparedStatement create = createClaim.createStatement();
				create.setInt(1, chunk.getX());
				create.setInt(2, chunk.getZ());
				
				create.executeUpdate();
				
				if (create.executeUpdate() > 0)
					PvPFaction.getInstance().getLogger().info("Claim created for " + chunk.getX() + ", " + chunk.getZ());
				else
					PvPFaction.getInstance().getLogger().severe("Trying to create claim for " + chunk.getX() + ", " + chunk.getZ() + " but nothing happened!");
				
				try(ResultSet result = create.getGeneratedKeys()){
					if (result.next())
						claims.put(new ChunkId(chunk), claim = new FactionClaim(result.getInt(1), null, null, null));
				}
			}
			return claim;
			
		} catch (SQLException e) {
			PvPFaction.getInstance().getLogger().warning("Failed to LOAD claim at " + chunk.getX() + ", " + chunk.getZ());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get all claims for specified faction.
	 * @param faction
	 * @return
	 */
	public Set<FactionClaim> fromFaction(Faction faction) {
		if (fullyLoadedFactionsClaims.contains(faction))
			return claims.asMap().values().stream().filter(claim -> faction.equals(claim.getFaction())).collect(Collectors.toSet());
		
		try (PreparedStatement statement = selectClaimsByFaction.createStatement()) {
			statement.setInt(1, faction.getID());
			ResultSet resultSet = statement.executeQuery();

			Set<FactionClaim> factionClaims = new HashSet<>();
			while (resultSet.next())
				factionClaims.add(toFactionClaim(resultSet));
			
			fullyLoadedFactionsClaims.add(faction);
			return factionClaims;
			
		} catch (SQLException e) {
			PvPFaction.getInstance().getLogger().warning("Failed to LOAD claims of " + faction.getName());
			e.printStackTrace();
			return null;
		}
	}

	
	private FactionClaim toFactionClaim(ResultSet resultSet) throws SQLException {
		FactionClaim claim = new FactionClaim(
				resultSet.getInt("id"),
				resultSet.getInt("faction_id"),
				resultSet.getString("members_factions"),
				resultSet.getString("members_players"));
		
		claims.put(new ChunkId(resultSet.getInt("x"), resultSet.getInt("z")), claim);
		return claim;
	}

	/*
	public void addClaim(FactionClaim factionClaim) throws SQLException {
		claims.put(factionClaim.getChunk(), factionClaim);
		updateClaim(factionClaim);
	}*/

	/*
	public void forceClaim(Chunk chunk, FactionClaimType type) throws SQLException {
		FactionClaim claim = new FactionClaim(chunk, type);
		claims.put(chunk, claim);
		insertClaim(claim);
	}*/

	/*
	public Set<FactionClaim> getChunksAround(Chunk chunk) {
		World world = chunk.getWorld();
		Set<FactionClaim> claims = new HashSet<>();
		claims.add(getByChunk(world.getChunkAt(chunk.getX() + 1, chunk.getX() + 1)));
		claims.add(getByChunk(world.getChunkAt(chunk.getX() + 1, chunk.getZ())));
		claims.add(getByChunk(world.getChunkAt(chunk.getX(), chunk.getZ() + 1)));
		claims.add(getByChunk(world.getChunkAt(chunk.getX() - 1, chunk.getX() - 1)));
		return claims;
	}*/

	/*public FactionClaim getByChunk(Chunk chunk) {
		try {
			FactionClaim claim = claims.getIfPresent(chunk);
			if (claim == null) {
				claim = fromChunk(chunk);
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
	}*/

	/*
	private void entityDamage(EntityDamageEvent e) {
		FactionClaim claim = ofChunk(e.getEntity().getLocation().getChunk());
		if (claim.getPlayerPerm(AccountProvider.get(e.getEntity().getUniqueId())).)
			e.setCancelled(true);
	}

	private void blockDamage(Cancellable e, Player p, Location location) {
		if (p == null)
			return;
		FactionClaim claim = getByChunk(p.getLocation().getChunk());
		if (claim.getType() == FactionClaimType.WILDERNESS) {
			if (claim.getFaction() == null)
				return;
			Faction faction = FactionPlayer.get(p).getClan();
			if (faction == claim.getFaction())
				return; // TODO voir autorisations par joueur
		}
		e.setCancelled(true);
		Prefix.FACTION.sendMessage(p, "&cImpossible de casser ou de poser un bloc dans ce claim !");
	}

	private void blockInteract(PlayerInteractEvent e) {
		Location location = e.getClickedBlock() != null ? e.getClickedBlock().getLocation() : null;
		if (location == null)
			location = e.getPlayer().getLocation();
		FactionClaim claim = getByChunk(location.getChunk());
		if (claim.getType() == FactionClaimType.WILDERNESS) {
			if (claim.getFaction() == null)
				return;
			Faction faction = FactionPlayer.get(e.getPlayer()).getClan();
			if (faction == claim.getFaction())
				return; // TODO voir autorisations par joueur
		}
		e.setCancelled(true);
		Prefix.FACTION.sendMessage(e.getPlayer(), "&cImpossible d'interagir avec les blocs dans ce claim !");
	}*/

	/*
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		claims.invalidate(event.getChunk());
	}*/

}
