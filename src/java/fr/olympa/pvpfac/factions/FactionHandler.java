package fr.olympa.pvpfac.factions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.olympa.pvpfac.factions.objects.OlympaFaction;

public class FactionHandler {

	private static List<OlympaFaction> cacheFactions = new ArrayList<>();
	private static Map<Player, Integer> players = new HashMap<>();

	public static OlympaFaction getFaction(int id) {
		return cacheFactions.stream().filter(f -> f.getId() == id).findFirst().orElse(null);
	}

	public static OlympaFaction getFaction(String name) {
		return cacheFactions.stream().filter(f -> f.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public static OlympaFaction getFactionByTag(String tag) {
		return cacheFactions.stream().filter(f -> f.getTag().equalsIgnoreCase(tag)).findFirst().orElse(null);
	}

	public static void addFaction(OlympaFaction faction) {
		FactionHandler.cacheFactions.add(faction);
	}

	public static void removeFaction(OlympaFaction faction) {
		FactionHandler.cacheFactions.remove(faction);
	}

	public static OlympaFaction getCacheFaction(Player player) {
		return cacheFactions.stream().filter(f -> f.getMembers().containsKey(player.getUniqueId())).findFirst().orElse(null);
	}

	public static OlympaFaction getFaction(Player player) {
		return getFaction(players.get(player));
	}

	public static void addPlayer(Player player, OlympaFaction faction) {
		FactionHandler.players.put(player, faction.getId());
	}

	public static void removePlayer(Player player) {
		FactionHandler.players.remove(player);
	}
}
