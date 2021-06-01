package fr.olympa.pvpfac.faction.map;

import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.pvpfac.player.FactionPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AutoMapListener implements Listener {

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		FactionMap.autoMapPlayers.remove(player);
	}

	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent event) {
		final Location from = event.getFrom();
		final Location to = event.getTo();
		final Player player = event.getPlayer();
		if (!FactionMap.autoMapPlayers.contains(player) || SpigotUtils.isSameChunk(from.getChunk(), to.getChunk())) return;
		FactionMap.sendMap(player, ((FactionPlayer) AccountProvider.get(player.getUniqueId())).getClan());
	}

}
