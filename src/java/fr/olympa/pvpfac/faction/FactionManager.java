package fr.olympa.pvpfac.faction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.StringJoiner;

import org.bukkit.Chunk;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.PvPFactionPermission;

public class FactionManager extends ClansManager<Faction> {
	
	public FactionManager() throws SQLException, ReflectiveOperationException {
		super(PvPFaction.getInstance(), "pvpfac_faction", 5);
		new FactionCommand<>(this, "faction", "Permet de gérer les factions.", PvPFactionPermission.FACTION_PLAYERS_COMMAND, "factions", "f", "fac").register();
	}
	
	@Override
	protected Faction createClan(int id, String name, long chief, int maxSize) {
		return new Faction(this, id, name, chief, maxSize);
	}
	
	public Faction getByChunk(Chunk chunk) {
		return getClans().stream().filter(c -> c.getValue().hasClaim(chunk)).map(e -> e.getValue()).findFirst().orElse(null);
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
		return columnsJoiner;
	}
	
	@Override
	protected Faction provideClan(int id, String name, long chief, int maxSize, double money, long created, ResultSet resultSet) throws SQLException {
		Set<FactionClaim> claims = new Gson().fromJson(resultSet.getString("claims"), new TypeToken<Set<FactionClaim>>() {
		}.getType());
		return new Faction(this, id, name, chief, maxSize, money, created, resultSet.getString("tag"), resultSet.getString("description"), SpigotUtils.convertStringToLocation(resultSet.getString("home")), claims);
	}
	
}