package fr.olympa.pvpfac.factions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.pvpfac.factions.FactionHandler;
import fr.olympa.pvpfac.factions.objects.OlympaFaction;

public class FactionJoinListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		OlympaFaction faction = FactionHandler.getCacheFaction(player);

		if (faction != null) {
			FactionHandler.addPlayer(player, faction);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		FactionHandler.removePlayer(player);
	}
}
