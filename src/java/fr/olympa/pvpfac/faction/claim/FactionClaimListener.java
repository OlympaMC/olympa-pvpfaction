package fr.olympa.pvpfac.faction.claim;

import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onDamageByEntity(EntityDamageByEntityEvent e) {		
		Entity target = e.getEntity();
		Entity damager = e.getDamager();
		
		if (damager instanceof Projectile)
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
			}
		}else if (claim != null) {
			if (target.getType() == EntityType.ARMOR_STAND || target.getType() == EntityType.ITEM_FRAME || target.getType() == EntityType.PAINTING) 
				manageClaimAction(e.getEntity().getLocation(),damagerFp.getPlayer(), e, 
						ClaimPermLevel::canInterractContainers, 
						"§cpas touche à ce qui ne t'appartient pas !");
			else
				manageClaimAction(e.getEntity().getLocation(),damagerFp.getPlayer(), e, 
						ClaimPermLevel::canDamageEntities, 
						"§7Tu ne peux pas attaquer d'entités dans ce claim.");
		}	
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		FactionClaim claim = PvPFaction.getInstance().getClaimsManager().fromChunk(e.getEntity().getLocation().getChunk());
		if (claim.getType() == null)
			return;
		
		if (!claim.getType().canPvp())
			e.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
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


	/////////////////////////////////////////////////////////////
	//                      BUILD EVENTS                       //
	/////////////////////////////////////////////////////////////
	
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		manageClaimAction(e.getBlock().getLocation(), e.getPlayer(), e, 
				ClaimPermLevel::canBuild, 
				"§cTu ne peux pas construire ici.");
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		manageClaimAction(e.getBlock().getLocation(), e.getPlayer(), e, 
				ClaimPermLevel::canBuild, 
				"§cTu ne peux pas construire ici.");
	}
	


	/////////////////////////////////////////////////////////////
	//                    INTERRACT EVENTS                     //
	/////////////////////////////////////////////////////////////
	

	@EventHandler(ignoreCancelled = true)
	public void onUseBucket(PlayerBucketEvent e) {
		manageClaimAction(e.getBlock().getLocation(), e.getPlayer(), e, 
				ClaimPermLevel::canBuild, 
				"§cNon, tu ne feras pas ça ici !");
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onInterractBlock(PlayerInteractEvent e) {		
		if (e.getClickedBlock() == null || 
				e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getPlayer().isSneaking())
			return;
		
		if (e.getClickedBlock() instanceof Container)
			manageClaimAction(e.getClickedBlock().getLocation(), e.getPlayer(), e, 
					ClaimPermLevel::canInterractContainers, 
					"§cPas touche à ce qui ne t'appartient pas !");
		
		if (e.getClickedBlock() instanceof Openable)
			manageClaimAction(e.getClickedBlock().getLocation(), e.getPlayer(), e, 
					ClaimPermLevel::canInterractDoors, 
					"§cTu ne peux pas passer par ici.");
		
		if (e.getClickedBlock() instanceof Powerable)
			manageClaimAction(e.getClickedBlock().getLocation(), e.getPlayer(), e, 
					ClaimPermLevel::canInterractDoors, 
					"§cTu ne peux pas activer de redstone dans cette zone.");
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onInterractArmorstand(PlayerArmorStandManipulateEvent e) {
		manageClaimAction(e.getRightClicked().getLocation(), e.getPlayer(), e, 
				ClaimPermLevel::canInterractContainers, 
				"§cpas touche à ce qui ne t'appartient pas !");
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBreakItemframe(HangingBreakByEntityEvent e) {
		if (e.getRemover().getType() == EntityType.ARROW)
			e.setCancelled(true);
		
		else if (e.getRemover() instanceof Player)
			manageClaimAction(e.getEntity().getLocation(), (Player) e.getRemover(), e, 
					ClaimPermLevel::canBuild, 
					"§cTu ne peux pas prendre cet item.");
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onInterractItemframe(HangingPlaceEvent e) {
		manageClaimAction(e.getEntity().getLocation(), e.getPlayer(), e, 
				ClaimPermLevel::canBuild, 
				"§cTu ne peux pas placer ça ici.");
	}
	
	


	/////////////////////////////////////////////////////////////
	//                 PROTECTED CHUNK EVENTS                  //
	/////////////////////////////////////////////////////////////

	@EventHandler
	public void onBlockFade(BlockFadeEvent e) {
		cancelIfChunkProtected(e.getBlock().getLocation(), e);
	}
	
	@EventHandler
	public void onBlockExplode(BlockExplodeEvent e) {
		cancelIfChunkProtected(e.getBlock().getLocation(), e);
	}

	@EventHandler
	public void onBlockFade(BlockBurnEvent e) {
		cancelIfChunkProtected(e.getBlock().getLocation(), e);
	}

	@EventHandler
	public void onBlockFade(CauldronLevelChangeEvent e) {
		cancelIfChunkProtected(e.getBlock().getLocation(), e);
	}

	@EventHandler
	public void onBlockFade(LeavesDecayEvent e) {
		cancelIfChunkProtected(e.getBlock().getLocation(), e);
	}
	
	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent e) {
		if (e.getSpawnReason() == SpawnReason.CUSTOM)
			return;
		
		cancelIfChunkProtected(e.getLocation(), e);
	}
	
	
	
	
	private void cancelIfChunkProtected(Location loc, Cancellable e) {
		FactionClaim claim = PvPFaction.getInstance().getClaimsManager().fromChunk(loc.getChunk());
		if (claim.getType() == null)
			return;
		
		if (claim.getType().isProtected())
			e.setCancelled(true);
	}
	
	private void manageClaimAction(Location loc, Player p, Cancellable event, Function<ClaimPermLevel, Boolean> method, String denyMessage, Object...args) {
		if (!method.apply(PvPFaction.getInstance().getClaimsManager().fromChunk(loc.getChunk()).getPlayerPerm(AccountProvider.get(p.getUniqueId())))) {
			Prefix.FACTION.sendMessage(p, denyMessage, args);
			event.setCancelled(true);
		}
	}
	
	
	
	
}
