package fr.olympa.pvpfac.faction.claim;

import org.bukkit.Chunk;
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
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
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
		victimfaction = victimfp.getClan();
		if (victimfaction == null)
			return;
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
		Faction attackerfaction = attackerfp.getClan();
		if (attackerfaction == null)
			return;
		if (!victimfaction.isSameClan(attackerfaction)) {
			Chunk victimChunk = victim.getLocation().getChunk();
			Faction factionvictimChunk = PvPFaction.getInstance().getFactionManager().getByChunk(victimChunk);
			if (factionvictimChunk.isSameClan(victimfaction)) {
				Prefix.FACTION.sendMessage(attacker, "&cImpossible d'attaquer &4%s&c dans son claim.", victim.getName());
				event.setCancelled(false);
			}
		} else {
			Prefix.FACTION.sendMessage(attacker, "&cAttaque pas le collègue &4%s&c !", victim.getName());
			event.setCancelled(false);
		}
	}

	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		Material material = event.getBucket();
		if (!material.equals(Material.LAVA_BUCKET))
			return;
		Location location = event.getBlockClicked().getLocation();
		FactionPlayer attackerfp = AccountProvider.get(player.getUniqueId());
		Faction faction = attackerfp.getClan();
		if (faction.getOnlineFactionPlayers().stream().filter(p -> SpigotUtils.playerisIn(p.getPlayer(), location)).findFirst().isPresent()) {
			player.sendMessage(ColorUtils.color(Prefix.FACTION + "Brûle pas le collègue !"));
			event.setCancelled(true);
			player.updateInventory();
		}
	}
}
