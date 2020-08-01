package fr.olympa.pvpfac.faction.claim;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.faction.FactionManager;

public class FactionClaimEnterListener implements Listener {

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		PvPFaction.getInstance().getFactionManager().removeCache(chunk);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		Chunk to = player.getLocation().getChunk();
		FactionManager manager = PvPFaction.getInstance().getFactionManager();
		Faction fChunkTo = manager.getByChunk(to);
		if (fChunkTo != null)
			player.sendTitle(fChunkTo.getNameColored(player.getUniqueId()), "§7" + fChunkTo.getDescription(), 0, 20, 20);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		if (SpigotUtils.isSameChunk(from.getChunk(), to.getChunk()))
			return;
		Player player = event.getPlayer();
		FactionManager manager = PvPFaction.getInstance().getFactionManager();
		Faction fChunkTo = manager.getByChunk(to.getChunk());
		if (fChunkTo != null) {
			Faction fChunkFrom = manager.getByChunk(from.getChunk());
			if (fChunkFrom != null && fChunkFrom.getID() == fChunkTo.getID())
				return;
			player.sendTitle(fChunkTo.getNameColored(player.getUniqueId()), "§7" + fChunkTo.getDescription(), 0, 20, 20);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		if (SpigotUtils.isSameChunk(from.getChunk(), to.getChunk()))
			return;
		Player player = event.getPlayer();
		FactionManager manager = PvPFaction.getInstance().getFactionManager();
		Faction fChunkTo = manager.getByChunk(to.getChunk());
		if (fChunkTo != null) {
			Faction fChunkFrom = manager.getByChunk(from.getChunk());
			if (fChunkFrom != null && fChunkFrom.getID() == fChunkTo.getID())
				return;
			player.sendTitle(fChunkTo.getNameColored(player.getUniqueId()), "§7" + fChunkTo.getDescription(), 0, 20, 20);
		}
	}
}
