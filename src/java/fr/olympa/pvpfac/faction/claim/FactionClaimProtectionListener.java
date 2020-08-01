package fr.olympa.pvpfac.faction.claim;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.faction.FactionManager;
import fr.olympa.pvpfac.player.FactionPlayer;

public class FactionClaimProtectionListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location blockLocation = block.getLocation();
		FactionManager manager = PvPFaction.getInstance().getFactionManager();
		Faction fBlockPlaced = manager.getByChunk(blockLocation.getChunk());
		if (fBlockPlaced == null)
			return;
		Faction fplayer = ((FactionPlayer) AccountProvider.get(player.getUniqueId())).getClan();
		if (fplayer == null || !fBlockPlaced.isSameClan(fplayer)) {
			Prefix.FACTION.sendMessage(player, "&cImpossible de poser un block dans le claim des &4%s&c !", fBlockPlaced.getName());
			event.setCancelled(false);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location blockLocation = block.getLocation();
		FactionManager manager = PvPFaction.getInstance().getFactionManager();
		Faction fBlockPlaced = manager.getByChunk(blockLocation.getChunk());
		if (fBlockPlaced == null)
			return;
		Faction fplayer = ((FactionPlayer) AccountProvider.get(player.getUniqueId())).getClan();
		if (fplayer == null || !fBlockPlaced.isSameClan(fplayer)) {
			Prefix.FACTION.sendMessage(player, "&cImpossible de détruire un block dans le claim des &4%s&c !", fBlockPlaced.getName());
			event.setCancelled(false);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		Location blockLocation = block.getLocation();
		FactionManager manager = PvPFaction.getInstance().getFactionManager();
		Faction fBlockPlaced = manager.getByChunk(blockLocation.getChunk());
		if (fBlockPlaced == null)
			return;
		Faction fplayer = ((FactionPlayer) AccountProvider.get(player.getUniqueId())).getClan();
		if (fplayer == null || !fBlockPlaced.isSameClan(fplayer)) {
			Prefix.FACTION.sendMessage(player, "&cImpossible d'intéragir dans le claim des &4%s&c !", fBlockPlaced.getName());
			event.setCancelled(false);
		}
	}
}
