package fr.olympa.pvpfac.tpa;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class TpaListener implements Listener {

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		TpaHandler.removeAllRequests(event.getPlayer());
	}

}
