package fr.olympa.pvpfac.factions.objects;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class OlympaFaction {

	int id;
	String name;
	String tag;
	String description;
	Map<UUID, OlympaFactionRole> members;
	List<?> chunks;
	Location home;
	long created;

	public OlympaFaction(String name, String tag, Player author) {
		this.name = name;
		this.tag = tag;
		members.put(author.getUniqueId(), OlympaFactionRole.CHEF);
	}

	public List<?> getChunks() {
		return chunks;
	}

	public String getDescription() {
		return description;
	}

	public Location getHome() {
		return home;
	}

	public int getId() {
		return id;
	}

	public Map<UUID, OlympaFactionRole> getMembers() {
		return members;
	}

	public String getName() {
		return name;
	}

	public String getTag() {
		return tag;
	}
}
