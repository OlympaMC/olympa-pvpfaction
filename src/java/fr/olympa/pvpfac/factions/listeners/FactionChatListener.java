package fr.olympa.pvpfac.factions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.olympa.pvpfac.factions.FactionHandler;
import fr.olympa.pvpfac.factions.objects.OlympaFaction;

public class FactionChatListener implements Listener {

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		OlympaFaction faction = FactionHandler.getFaction(player);
		if (faction != null) {
			event.setFormat(faction.getName() + " %s : %s");
		}
	}
}
