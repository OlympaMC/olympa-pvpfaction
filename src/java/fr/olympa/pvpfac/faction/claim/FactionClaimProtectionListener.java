package fr.olympa.pvpfac.faction.claim;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.player.FactionPlayer;

public class FactionClaimProtectionListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location blockLocation = block.getLocation();
		FactionClaimsManager manager = PvPFaction.getInstance().getClaimsManager();
		FactionClaim factionClaim;
		factionClaim = manager.getByChunk(blockLocation.getChunk());
		if (factionClaim == null) {
			Prefix.FACTION.sendMessage(player, "&4Impossible de charger le claim.");
			event.setCancelled(true);
			return;
		}
		Faction fplayer = ((FactionPlayer) AccountProvider.get(player.getUniqueId())).getClan();
		if (fplayer == null || !factionClaim.canInteract(fplayer)) {
			Prefix.FACTION.sendMessage(player, "&cImpossible de poser un block dans ce claim !");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location blockLocation = block.getLocation();

		FactionClaimsManager manager = PvPFaction.getInstance().getClaimsManager();
		FactionClaim factionClaim;
		factionClaim = manager.getByChunk(blockLocation.getChunk());
		if (factionClaim == null) {
			Prefix.FACTION.sendMessage(player, "&4Impossible de charger le claim.");
			event.setCancelled(true);
			return;
		}
		Faction fplayer = ((FactionPlayer) AccountProvider.get(player.getUniqueId())).getClan();
		if (fplayer == null || !factionClaim.canInteract(fplayer)) {
			Prefix.FACTION.sendMessage(player, "&cImpossible de détruire un block dans ce claim !");
			event.setCancelled(true);
		}
	}
}
