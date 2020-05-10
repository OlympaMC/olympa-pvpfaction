package fr.olympa.pvpfac.factionold.objects;

import org.bukkit.entity.Player;

public class OlympaFactionInvite {

	Player author;
	OlympaFaction faction;

	public OlympaFactionInvite(Player author, OlympaFaction faction) {
		this.author = author;
		this.faction = faction;
	}

	public Player getAuthor() {
		return author;
	}

	public OlympaFaction getFaction() {
		return faction;
	}
}
