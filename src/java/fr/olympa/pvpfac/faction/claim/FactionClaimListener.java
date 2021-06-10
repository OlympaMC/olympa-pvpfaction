package fr.olympa.pvpfac.faction.claim;

import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.player.FactionPlayer;
import fr.olympa.pvpfac.world.WorldsManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.ArmorStand;
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
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FactionClaimListener implements Listener {

	/////////////////////////////////////////////////////////////
	//                   ENTER CLAIM EVENTS                    //
	/////////////////////////////////////////////////////////////

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
		final Player player = event.getPlayer();
		final Chunk to = player.getLocation().getChunk();

		if (!WorldsManager.CLAIM_WORLD.getWorld().getUID().equals(to.getWorld().getUID())) return;

		final FactionClaimsManager manager = PvPFaction.getInstance().getClaimsManager();
		final FactionClaim factionClaim;
		try {
			factionClaim = manager.ofChunk(to);
			if (factionClaim != null) {
				factionClaim.sendTitle(player);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			player.sendTitle("§4Erreur", "§cImpossible de charger ce claim", 0, 20, 20);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(final PlayerMoveEvent e) {
		if (!WorldsManager.CLAIM_WORLD.getWorld().getUID().equals(e.getPlayer().getLocation().getWorld().getUID())) return;

		final Chunk from = e.getFrom().getChunk();
		final Chunk to = e.getTo().getChunk();

		if (SpigotUtils.isSameChunk(from, to)) return;

		try {
			final FactionClaim oldClaim = PvPFaction.getInstance().getClaimsManager().ofChunk(from);
			final FactionClaim newClaim = PvPFaction.getInstance().getClaimsManager().ofChunk(to);
			//System.out.println(oldClaim);
			//System.out.println(newClaim);
			if (oldClaim.hasSameFaction(newClaim)) return;

			newClaim.sendTitle(e.getPlayer());
		} catch (final Exception ex) {
			ex.printStackTrace();
			e.getPlayer().sendTitle("§4Erreur", "§cImpossible de charger ce claim", 0, 20, 20);
		}
	}

	/////////////////////////////////////////////////////////////
	//                     DAMAGE EVENTS                       //
	/////////////////////////////////////////////////////////////

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onDamageByEntity(final EntityDamageByEntityEvent e) {
		if (!WorldsManager.CLAIM_WORLD.getWorld().getUID().equals(e.getEntity().getLocation().getWorld().getUID())) return;

		final Entity target = e.getEntity();
		Entity damager = e.getDamager();

		if (damager instanceof Projectile) damager = (Entity) ((Projectile) damager).getShooter();

		if (damager.getType() != EntityType.PLAYER) return;

		final FactionPlayer damagerFactionPlayer = AccountProvider.get(damager.getUniqueId());
		final FactionClaim damagerClaim = PvPFaction.getInstance().getClaimsManager().ofChunk(damager.getLocation().getChunk());
		if (damagerClaim.getType() != null && !damagerClaim.getType().canPvp()) {
			e.setCancelled(true);
			return;
		}

		final FactionClaim claim = PvPFaction.getInstance().getClaimsManager().ofChunk(e.getEntity().getLocation().getChunk());

		if (target.getType() == EntityType.PLAYER) {
			final FactionPlayer targetFactionPlayer = AccountProvider.get(target.getUniqueId());

			if (targetFactionPlayer.getClan() != null &&
			    (targetFactionPlayer.getClan().equals(damagerFactionPlayer.getClan()) || targetFactionPlayer.getClan().isAlly(damagerFactionPlayer.getClan()))) {
				Prefix.FACTION.sendMessage(damager, "§7On attaque pas le copain !");
				e.setCancelled(true);
			}
		} else if (claim != null) {
			if (target.getType() == EntityType.ARMOR_STAND || target.getType() == EntityType.ITEM_FRAME || target.getType() == EntityType.PAINTING) {
				isActionCancelled(
					e.getEntity().getLocation(), damagerFactionPlayer.getPlayer(), e,
					FactionClaimPermLevel::canInteractContainers,
					"§cPas touche à ce qui ne t'appartient pas !"
				);
			} else {
				isActionCancelled(
					e.getEntity().getLocation(), damagerFactionPlayer.getPlayer(), e,
					FactionClaimPermLevel::canDamageEntities,
					"§7Tu ne peux pas attaquer d'entités dans ce claim."
				);
			}
		}
	}

	/**
	 * Return true if event was cancelled, false otherwise.
	 */
	private boolean isActionCancelled(final Location loc, final Player p, final Cancellable event, final Function<FactionClaimPermLevel, Boolean> method, final String denyMessage) {
		if (!WorldsManager.CLAIM_WORLD.getWorld().getUID().equals(loc.getWorld().getUID())) return false;

		if (!method.apply(PvPFaction.getInstance().getClaimsManager().ofChunk(loc.getChunk()).getPlayerPerm(AccountProvider.get(p.getUniqueId())))) {
			Prefix.FACTION.sendMessage(p, denyMessage);
			//Bukkit.broadcastMessage("CANCELLED : " + event + " triggered by " + p);
			event.setCancelled(true);
			return true;
		}

		return false;
	}

	@EventHandler
	public void onDamage(final EntityDamageEvent e) {
		if (!WorldsManager.CLAIM_WORLD.getWorld().getUID().equals(e.getEntity().getLocation().getWorld().getUID())) return;

		final FactionClaim claim = PvPFaction.getInstance().getClaimsManager().ofChunk(e.getEntity().getLocation().getChunk());
		if (claim.getType() != null && !claim.getType().canPvp()) e.setCancelled(true);
	}


	/////////////////////////////////////////////////////////////
	//                      BUILD EVENTS                       //
	/////////////////////////////////////////////////////////////

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent e) {
		if (!WorldsManager.CLAIM_WORLD.getWorld().getUID().equals(e.getBlock().getLocation().getWorld().getUID())) return;

		final Player p = e.getPlayer();
		final Material material = e.getBucket();
		if (!material.equals(Material.LAVA_BUCKET)) {
			return;
		}

		final Location location = e.getBlockClicked().getLocation();
		final Faction faction = ((FactionPlayer) AccountProvider.get(p.getUniqueId())).getClan();

		//if (faction != null && faction.getOnlineFactionPlayers().stream().filter(player -> SpigotUtils.playerisIn(player.getPlayer(), location)).findFirst().isPresent()) {
		if (
			faction != null && Bukkit.getOnlinePlayers().stream().anyMatch(
				pl ->
					SpigotUtils.playerisIn(pl, location) &&
					faction.isAlly(((FactionPlayer) AccountProvider.get(pl.getUniqueId())).getClan())
			)
		) {
			Prefix.FACTION.sendMessage(p, "Eh, brûle pas le collègue !");
			e.setCancelled(true);
			p.updateInventory();
		}
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent e) {
		if (!WorldsManager.CLAIM_WORLD.getWorld().getUID().equals(e.getBlock().getLocation().getWorld().getUID())) return;

		if (!isActionCancelled(
			e.getBlock().getLocation(), e.getPlayer(), e,
			FactionClaimPermLevel::canBuild,
			"§cTu ne peux pas construire ici."
		) && e.getBlock() instanceof Container) {

			final FactionClaim claim = PvPFaction.getInstance().getClaimsManager().ofChunk(e.getBlock().getLocation().getChunk());
			if (!claim.getType().canPlaceContainers()) {
				e.setCancelled(true);
				Prefix.FACTION.sendMessage(e.getPlayer(), "§cTu ne peux pas utiliser de coffre dans les AP. §7Utilise des portes-armure à la place !");
			}

		}
	}


	/////////////////////////////////////////////////////////////
	//                    INTERACT EVENTS                     //
	/////////////////////////////////////////////////////////////

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent e) {
		isActionCancelled(
			e.getBlock().getLocation(), e.getPlayer(), e,
			FactionClaimPermLevel::canBuild,
			"§cTu ne peux pas construire ici."
		);
	}

	@EventHandler(ignoreCancelled = true)
	public void onUseBucket(final PlayerBucketFillEvent e) {
		isActionCancelled(
			e.getBlock().getLocation(), e.getPlayer(), e,
			FactionClaimPermLevel::canBuild,
			"§cNon, tu ne feras pas ça ici !"
		);
	}

	@EventHandler(ignoreCancelled = true)
	public void onUseBucket(final PlayerBucketEmptyEvent e) {
		isActionCancelled(
			e.getBlock().getLocation(), e.getPlayer(), e,
			FactionClaimPermLevel::canBuild,
			"§cNon, tu ne feras pas ça ici !"
		);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInteractBlock(final PlayerInteractEvent e) {
		if (!WorldsManager.CLAIM_WORLD.getWorld().getUID().equals(e.getPlayer().getLocation().getWorld().getUID())) return;

		final FactionClaim claim = PvPFaction.getInstance().getClaimsManager().ofChunk(
			e.getInteractionPoint() == null ? e.getPlayer().getLocation().getChunk() : e.getInteractionPoint().getChunk());

		if (claim.getType().isProtected()) {
			e.setCancelled(true);
			return;
		}

		if (e.getClickedBlock() == null ||
		    e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getPlayer().isSneaking()) {
			return;
		}

		if (e.getClickedBlock() instanceof Container) {
			isActionCancelled(
				e.getClickedBlock().getLocation(), e.getPlayer(), e,
				FactionClaimPermLevel::canInteractContainers,
				"§cPas touche à ce qui ne t'appartient pas !"
			);
		}

		if (e.getClickedBlock() instanceof Openable) {
			isActionCancelled(
				e.getClickedBlock().getLocation(), e.getPlayer(), e,
				FactionClaimPermLevel::canInteractDoors,
				"§cTu ne peux pas passer par ici..."
			);
		}

		if (e.getClickedBlock() instanceof Powerable) {
			isActionCancelled(
				e.getClickedBlock().getLocation(), e.getPlayer(), e,
				FactionClaimPermLevel::canInteractDoors,
				"§cTu ne peux pas activer de redstone dans cette zone."
			);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInteractArmorstand(final PlayerArmorStandManipulateEvent e) {
		isActionCancelled(
			e.getRightClicked().getLocation(), e.getPlayer(), e,
			FactionClaimPermLevel::canInteractContainers,
			"§cpas touche à ce qui ne t'appartient pas !"
		);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBreakItemframe(final HangingBreakByEntityEvent e) {
		if (!WorldsManager.CLAIM_WORLD.getWorld().getUID().equals(e.getEntity().getLocation().getWorld().getUID())) {
			return;
		}

		if (e.getRemover().getType() == EntityType.ARROW) {
			e.setCancelled(true);
		} else if (e.getRemover() instanceof Player) {
			isActionCancelled(
				e.getEntity().getLocation(), (Player) e.getRemover(), e,
				FactionClaimPermLevel::canBuild,
				"§cTu ne peux pas prendre cet item."
			);
		}
	}


	/////////////////////////////////////////////////////////////
	//                 PROTECTED CHUNK EVENTS                  //
	/////////////////////////////////////////////////////////////

	@EventHandler(ignoreCancelled = true)
	public void onInteractItemframe(final HangingPlaceEvent e) {
		isActionCancelled(
			e.getEntity().getLocation(), e.getPlayer(), e,
			FactionClaimPermLevel::canBuild,
			"§cTu ne peux pas placer ça ici."
		);
	}

	@EventHandler
	public void onBlockExplode(final BlockExplodeEvent e) {
		if (!isModificationAllowed(e.blockList())) {
			e.setCancelled(true);
		}
	}

	private boolean isModificationAllowed(final List<Block> blocks) {
		for (final Chunk ch : blocks.stream().map(Block::getChunk).collect(Collectors.toSet())) {
			if (PvPFaction.getInstance().getClaimsManager().ofChunk(ch).getType().isProtected()) return false;
		}

		return true;
	}

	@EventHandler
	public void onEntityExplode(final EntityExplodeEvent e) {
		if (!isModificationAllowed(e.blockList())) e.setCancelled(true);
	}

	@EventHandler
	public void onPistonExtend(final BlockPistonExtendEvent e) {
		if (!isModificationAllowed(e.getBlocks())) e.setCancelled(true);
	}

	@EventHandler
	public void onPistonRetract(final BlockPistonRetractEvent e) {
		if (!isModificationAllowed(e.getBlocks())) e.setCancelled(true);
	}

	@EventHandler
	public void onBlockFade(final BlockFadeEvent e) {
		isClaimProtected(e.getBlock().getLocation(), e);
	}

	private boolean isClaimProtected(final Location loc, final Cancellable e) {
		if (!WorldsManager.CLAIM_WORLD.getWorld().getUID().equals(loc.getWorld().getUID())) return false;

		if (PvPFaction.getInstance().getClaimsManager().ofChunk(loc.getChunk()).getType().isProtected()) {
			e.setCancelled(true);
			return true;
		}

		return false;
	}

	@EventHandler
	public void onBlockBurn(final BlockBurnEvent e) {
		isClaimProtected(e.getBlock().getLocation(), e);
	}

	@EventHandler
	public void onBlockCauldron(final CauldronLevelChangeEvent e) {
		isClaimProtected(e.getBlock().getLocation(), e);
	}

	@EventHandler
	public void onBlockLeaves(final LeavesDecayEvent e) {
		isClaimProtected(e.getBlock().getLocation(), e);
	}

	@EventHandler
	public void onEntitySpawn(final CreatureSpawnEvent e) {
		if (e.getSpawnReason() == SpawnReason.CUSTOM) return;

		if (!isClaimProtected(e.getLocation(), e)) {
			if (e.getEntityType() != EntityType.ARMOR_STAND) return;
			final ArmorStand armorStand = (ArmorStand) e.getEntity();
			armorStand.setArms(true);
		}
	}

}
