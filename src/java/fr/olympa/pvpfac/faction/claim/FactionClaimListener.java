package fr.olympa.pvpfac.faction.claim;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.faction.FactionManager;

public class FactionClaimListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		if (SpigotUtils.isSameLocation(from, to))
			return;
		Player player = event.getPlayer();
		FactionManager manager = PvPFaction.getInstance().getFactionManager();
		Faction fChunkTo = manager.getByChunk(to.getChunk());
		if (fChunkTo != null) {
			Faction fChunkFrom = manager.getByChunk(from.getChunk());
			if (fChunkFrom != null && fChunkFrom.getID() == fChunkTo.getID())
				return;
			player.sendTitle(fChunkTo.getName(), fChunkTo.getDescription(), 0, 20, 20);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		if (SpigotUtils.isSameLocation(from, to))
			return;
		Player player = event.getPlayer();
		FactionManager manager = PvPFaction.getInstance().getFactionManager();
		Faction fChunkTo = manager.getByChunk(to.getChunk());
		if (fChunkTo != null) {
			Faction fChunkFrom = manager.getByChunk(from.getChunk());
			if (fChunkFrom != null && fChunkFrom.getID() == fChunkTo.getID())
				return;
			player.sendTitle(fChunkTo.getName(), fChunkTo.getDescription(), 0, 20, 20);
		}
	}
}
