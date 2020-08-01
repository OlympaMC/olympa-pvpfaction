package fr.olympa.pvpfac.faction.map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.player.FactionPlayer;

public class AutoMapListener implements Listener {

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		FactionMap.autoMapPlayers.remove(player);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		Player player = event.getPlayer();
		if (!FactionMap.autoMapPlayers.contains(player) || SpigotUtils.isSameChunk(from.getChunk(), to.getChunk()))
			return;
		FactionMap.sendMap(player, ((FactionPlayer) AccountProvider.get(player.getUniqueId())).getClan());
	}

}
