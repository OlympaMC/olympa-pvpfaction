package fr.olympa.pvpfac.faction;

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
import fr.olympa.pvpfac.player.FactionPlayer;

public class FactionPvPListener implements Listener {
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity entityVictim = event.getEntity();
		Entity entityAttacker = event.getDamager();
		Faction victimfaction;
		if (!(entityVictim instanceof Player))
			return;
		Player victim = (Player) entityVictim;
		FactionPlayer victimfp = AccountProvider.get(victim.getUniqueId());
		// victimfaction = victimfp.getFaction();
		// if (victimfaction == null) {
		// return;
		// }
		Player attacker = null;
		if (entityAttacker instanceof Player)
			attacker = (Player) entityAttacker;
		else if (entityAttacker instanceof Projectile) {
			// Projectile = arrow, all potions ...
			ProjectileSource shooter = ((Projectile) entityAttacker).getShooter();
			if (shooter instanceof Player)
				attacker = (Player) shooter;
		}
		if (attacker == null)
			return;

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
		if (!material.equals(Material.LAVA_BUCKET))
			return;
		Location location = event.getBlockClicked().getLocation();
		FactionPlayer attackerfp = AccountProvider.get(player.getUniqueId());
		// OlympaFaction faction = attackerfp.getFaction();
		//if (faction.getOnlinePlayers().stream().filter(p -> SpigotUtils.playerisIn(p, location)).findFirst().isPresent()) {
		//			player.sendMessage(ColorUtils.color(Prefix.FACTION + "Brûle pas le collègue !"));
		//			event.setCancelled(true);
		//		}
	}
}
