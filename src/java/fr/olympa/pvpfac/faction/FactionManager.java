package fr.olympa.pvpfac.faction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Chunk;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.sql.OlympaStatement.StatementType;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.PvPFactionPermission;
import fr.olympa.pvpfac.faction.claim.FactionClaim;

public class FactionManager extends ClansManager<Faction> {
	
	public Cache<FactionClaim, Faction> claimCache;

	public OlympaStatement updateFactionClaimsStatement;
	public OlympaStatement updateFactionHomeStatement;
	public OlympaStatement updateTagStatement;
	public OlympaStatement updateDescriptionStatement;
	public OlympaStatement createDefaultClanStatement;

	public FactionManager() throws SQLException, ReflectiveOperationException {
		super(PvPFaction.getInstance(), "pvpfac_faction", 10);
		new FactionCommand<>(this, "faction", "Permet de gérer les factions.", PvPFactionPermission.FACTION_PLAYERS_COMMAND, "factions", "f", "fac").register();
		updateFactionClaimsStatement = new OlympaStatement(StatementType.UPDATE, tableName, new String[] { "id" }, "claims");
		updateFactionHomeStatement = new OlympaStatement(StatementType.UPDATE, tableName, new String[] { "id" }, "home");
		updateTagStatement = new OlympaStatement(StatementType.UPDATE, tableName, new String[] { "id" }, "tag");
		updateDescriptionStatement = new OlympaStatement(StatementType.UPDATE, tableName, new String[] { "id" }, "description");
		createDefaultClanStatement = new OlympaStatement(StatementType.INSERT, tableName, "name", "chief", "description");
		claimCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
		Faction fac;
		for (FactionType defaultFac : FactionType.getDefaultFactions().stream().filter(ft -> !getClans().stream().anyMatch(entry -> entry.getValue().getType() == ft)).collect(Collectors.toList())) {
			
			PreparedStatement statement = createDefaultClanStatement.getStatement();
			int i = 1;
			statement.setString(i++, defaultFac.getDefaultName());
			statement.setLong(i++, 2);
			statement.setString(i++, defaultFac.getDefaultDesciption());
			ResultSet resultSet = statement.getGeneratedKeys();
			resultSet.next();
			int id = resultSet.getInt(1);
			resultSet.close();
			super.clans.put(id, new Faction(this, id, defaultFac.getDefaultName(), defaultFac.getDefaultDesciption(), 2, defaultFac));
		}
	}
	
	@Override
	protected Faction createClan(int id, String name, long chief, int maxSize) {
		return new Faction(this, id, name, chief, maxSize);
	}

	public void removeCache(Chunk chunk) {
		FactionClaim fc = claimCache.asMap().keySet().stream().filter(e -> e.isChunk(chunk)).findFirst().orElse(null);
		if (fc != null)
			claimCache.invalidate(fc);
	}
	
	public Faction getByChunk(Chunk chunk) {
		Faction faction = claimCache.asMap().entrySet().stream().filter(e -> e.getKey().isChunk(chunk)).map(e -> e.getValue()).findFirst().orElse(null);
		if (faction == null) {
			faction = getClans().stream().filter(c -> c.getValue().hasClaim(chunk)).map(e -> e.getValue()).findFirst().orElse(null);
			claimCache.put(faction.getClaim(chunk), faction);
		}
		return faction;
	}
	
	@Override
	public StringJoiner addDBCollums(StringJoiner columnsJoiner) {
		columnsJoiner = super.addDBCollums(columnsJoiner);
		columnsJoiner.add("`tag` VARCHAR(6) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'");
		columnsJoiner.add("`description` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'");
		columnsJoiner.add("`ally` TEXT(65535) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'");
		columnsJoiner.add("`truce` TEXT(65535) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'");
		columnsJoiner.add("`ennemy` TEXT(65535) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'");
		columnsJoiner.add("`type` INT(1) NULL DEFAULT '0'");
		columnsJoiner.add("`claims` TEXT(65535) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'");
		columnsJoiner.add("`type` TINYINT(1) NOT NULL DEFAULT '0'");
		return columnsJoiner;
	}
	
	@Override
	protected Faction provideClan(int id, String name, long chief, int maxSize, double money, long created, ResultSet resultSet) throws SQLException {
		String jsonClaims = resultSet.getString("claims");
		Set<FactionClaim> claims = new HashSet<>();
		if (jsonClaims != null && !jsonClaims.isBlank())
			claims = new Gson().fromJson(jsonClaims, new TypeToken<Set<FactionClaim>>() {
			}.getType());
		return new Faction(this, id, name, chief, maxSize, money, created, resultSet.getString("tag"), resultSet.getString("description"),
				SpigotUtils.convertStringToLocation(resultSet.getString("home")), claims, FactionType.get(resultSet.getInt("type")));
	}

	public Faction getByName(String name) {
		return getClans().stream().filter(c -> c.getValue().getName().equalsIgnoreCase(name)).map(c -> c.getValue()).findFirst().orElse(null);
	}
	
	public Faction getByTag(String tag) {
		return getClans().stream().filter(c -> c.getValue().getTag().equalsIgnoreCase(tag)).map(c -> c.getValue()).findFirst().orElse(null);
	}
	
	public Faction get(String nameOrTagOrPlayer) throws SQLException {
		Faction faction = getByName(nameOrTagOrPlayer);
		if (faction == null)
			faction = getByTag(nameOrTagOrPlayer);
		if (faction == null) {
			ClanPlayerInterface<Faction> target = AccountProvider.get(nameOrTagOrPlayer);
			faction = target.getClan();
		}
		return faction;
	}

}