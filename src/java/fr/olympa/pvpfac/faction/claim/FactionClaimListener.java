package fr.olympa.pvpfac.faction.claim;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.player.FactionPlayer;

public class FactionClaimListener implements Listener {

	/////////////////////////////////////////////////////////////
	//                   ENTER CLAIM EVENTS                    //
	/////////////////////////////////////////////////////////////
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		Chunk to = player.getLocation().getChunk();
		FactionClaimsManager manager = PvPFaction.getInstance().getClaimsManager();
		FactionClaim factionClaim;
		try {
			factionClaim = manager.fromChunk(to);
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
			FactionClaim oldFactionClaim = manager.fromChunk(from);
			factionClaim = manager.fromChunk(to);
			if (oldFactionClaim.hasSameFaction(factionClaim))
				return;
			factionClaim.sendTitle(player);
		} catch (Exception e) {
			e.printStackTrace();
			player.sendTitle("§4Erreur", "§cImpossible de charger ce claim", 0, 20, 20);
		}
	}

	/////////////////////////////////////////////////////////////
	//                     DAMAGE EVENTS                       //
	/////////////////////////////////////////////////////////////
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onDamageByEntity(EntityDamageByEntityEvent e) {		
		Entity target = e.getEntity();
		Entity damager = e.getDamager();
		
		if (damager instanceof Projectile && ((Projectile)damager).getShooter() instanceof Player)
			damager = (Entity) ((Projectile)damager).getShooter();
		
		if (damager.getType() != EntityType.PLAYER)
			return;

		FactionPlayer damagerFp = AccountProvider.get(damager.getUniqueId());
		FactionClaim claim = PvPFaction.getInstance().getClaimsManager().fromChunk(e.getEntity().getLocation().getChunk());
		
		if (target.getType() == EntityType.PLAYER) {
			FactionPlayer targetFp = AccountProvider.get(target.getUniqueId());
			
			if (targetFp.getClan() != null && (targetFp.getClan().equals(damagerFp.getClan()) || targetFp.getClan().isAlly(damagerFp.getClan()))) {
				Prefix.FACTION.sendMessage(damager, "§7On attaque pas le copain !");
				e.setCancelled(true);
				return;
			}
		}else if (claim != null) {
			if (target.getType() == EntityType.ARMOR_STAND || target.getType() == EntityType.ITEM_FRAME || target.getType() == EntityType.PAINTING) {
				if (!claim.getPlayerPerm(damagerFp).canBuild()) {
					Prefix.FACTION.sendMessage(damager, "§7Tu ne peux pas détruire de porte armure, de cadre ou de tableau dans ce claim.");
					e.setCancelled(true);
					return;
				}
			}else
				if (!claim.getPlayerPerm(damagerFp).canDamageEntities()) {
					Prefix.FACTION.sendMessage(damager, "§7Tu ne peux pas attaquer d'entités dans ce claim.");
					e.setCancelled(true);
					return;
				}
		}	
	}

	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Player p = event.getPlayer();
		Material material = event.getBucket();
		if (!material.equals(Material.LAVA_BUCKET))
			return;
		
		Location location = event.getBlockClicked().getLocation();
		Faction faction = ((FactionPlayer)AccountProvider.get(p.getUniqueId())).getClan();
		
		//if (faction != null && faction.getOnlineFactionPlayers().stream().filter(player -> SpigotUtils.playerisIn(player.getPlayer(), location)).findFirst().isPresent()) {
		if (faction != null && Bukkit.getOnlinePlayers().stream().filter(pl -> SpigotUtils.playerisIn(pl, location) && 
				faction.isAlly(((FactionPlayer)AccountProvider.get(pl.getUniqueId())).getClan())).findFirst().isPresent()) {
			Prefix.FACTION.sendMessage(p, "Eh, brûle pas le collègue !");
			event.setCancelled(true);
			p.updateInventory();
		}
	}
	
	
	
	
	
	
	
	
}
