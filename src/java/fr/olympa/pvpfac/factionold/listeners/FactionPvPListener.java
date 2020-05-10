package fr.olympa.pvpfac.factionold.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.projectiles.ProjectileSource;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.pvpfac.factionold.objects.FactionPlayer;
import fr.olympa.pvpfac.factionold.objects.OlympaFaction;

public class FactionPvPListener implements Listener {

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity entityVictim = event.getEntity();
		Entity entityAttacker = event.getDamager();
		OlympaFaction victimfaction;
		if (!(entityVictim instanceof Player)) {
			return;
		}
		Player victim = (Player) entityVictim;
		FactionPlayer victimfp = AccountProvider.get(victim.getUniqueId());
		// victimfaction = victimfp.getFaction();
		// if (victimfaction == null) {
		// return;
		// }
		Player attacker;
		if (entityAttacker instanceof Player) {
			attacker = (Player) entityAttacker;
		} else if (entityAttacker instanceof Projectile) {
			ProjectileSource shooter = ((Projectile) entityAttacker).getShooter();
			if (shooter instanceof Player) {
				attacker = (Player) shooter;
			} else {
				return;
			}
		} else {
			// TODO POTION
			return;
		}

		FactionPlayer attackerfp = AccountProvider.get(attacker.getUniqueId());
		// OlympaFaction attackerfaction = attackerfp.getFaction();
		// if (attackerfaction == null) {
		return;
		// } else if (victimfaction.getId() == attackerfaction.getId()) {
		// event.setCancelled(false);
		// }

	}

	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		Material material = event.getBucket();
		if (!material.equals(Material.LAVA_BUCKET)) {
			return;
		}
		Location location = event.getBlockClicked().getLocation();
		FactionPlayer attackerfp = AccountProvider.get(player.getUniqueId());
		// OlympaFaction faction = attackerfp.getFaction();
		//if (faction.getOnlinePlayers().stream().filter(p -> SpigotUtils.playerisIn(p, location)).findFirst().isPresent()) {
//			player.sendMessage(ColorUtils.color(Prefix.FACTION + "Brûle pas le collègue !"));
//			event.setCancelled(true);
//		}
	}
}
