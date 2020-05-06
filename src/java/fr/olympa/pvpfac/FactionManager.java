package fr.olympa.pvpfac;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.StringJoiner;

import org.bukkit.Chunk;

import fr.olympa.api.clans.ClansManager;

public class FactionManager extends ClansManager<Faction> {

	public FactionManager() throws SQLException, ReflectiveOperationException {
		super(PvPFaction.getInstance(), "pvpfac_faction", Collections.emptyList(), 5);
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
	protected Faction provideClan(int id, String name, long chief, int maxSize, long created, ResultSet resultSet) {
		return new Faction(this, id, name, chief, maxSize, created);
	}

	@Override
	public StringJoiner getExtraCollumsDb(StringJoiner columnsJoiner) {
		columnsJoiner.add("`tag` VARCHAR(6) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'");
		columnsJoiner.add("`description` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'");
		columnsJoiner.add("`ally` TEXT(65535) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'");
		columnsJoiner.add("`truce` TEXT(65535) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'");
		columnsJoiner.add("`ennemy` TEXT(65535) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'");
		columnsJoiner.add("`type` INT(1) NULL DEFAULT '0'");
		return columnsJoiner;
	}
}