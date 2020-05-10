package fr.olympa.pvpfac.factionold.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.pvpfac.factionold.FactionHandler;
import fr.olympa.pvpfac.factionold.objects.OlympaFaction;

public class FactionJoinListener implements Listener {

	{
		for (Player player : Bukkit.getOnlinePlayers()) {
			OlympaFaction faction = FactionHandler.getCacheFaction(player);
			if (faction != null) {
				faction.addConnected(player);
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		OlympaFaction faction = FactionHandler.getCacheFaction(player);
		if (faction != null) {
			faction.addConnected(player);
		}
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		OlympaFaction faction = FactionHandler.getCacheFaction(player);

		if (faction != null) {
			faction.removeConnected(player);
			if (faction.getOnlinePlayers().size() == 0) {
				FactionHandler.removeFaction(faction);
			}
		}
		FactionHandler.removePlayerTryingToCreateFaction(player);
	}
}
