package fr.olympa.pvpfac.faction.claim;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;

public class FactionClaimEnterListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		Chunk to = player.getLocation().getChunk();
		FactionClaimsManager manager = PvPFaction.getInstance().getClaimsManager();
		FactionClaim factionClaim;
		try {
			factionClaim = manager.getByChunk(to);
			if (factionClaim != null)
				factionClaim.sendTitle(player);
		} catch (Exception e) {
			e.printStackTrace();
			player.sendTitle("§4Erreur", "§cImpossible de charger ce claim", 0, 20, 20);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent event) {
		Chunk from = event.getFrom().getChunk();
		Chunk to = event.getTo().getChunk();
		if (SpigotUtils.isSameChunk(from, to))
			return;
		Player player = event.getPlayer();
		FactionClaimsManager manager = PvPFaction.getInstance().getClaimsManager();
		FactionClaim factionClaim;
		try {
			FactionClaim oldFactionClaim = manager.getByChunk(from);
			factionClaim = manager.getByChunk(to);
			if (oldFactionClaim.hasSameFaction(factionClaim))
				return;
			factionClaim.sendTitle(player);
		} catch (Exception e) {
			e.printStackTrace();
			player.sendTitle("§4Erreur", "§cImpossible de charger ce claim", 0, 20, 20);
		}
	}
}
