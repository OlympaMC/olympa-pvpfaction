package fr.olympa.pvpfac.faction.claim;

import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.player.FactionPlayer;
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

public class FactionPvPListener implements Listener {

	@EventHandler
	public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
		final Entity entityVictim = event.getEntity();
		final Entity entityAttacker = event.getDamager();
		final Faction victimFaction;
//		if (!(entityVictim instanceof final Player victim)) return;
		if (!(entityVictim instanceof Player victim)) return;

		final FactionPlayer victimFactionPlayer = AccountProvider.get(victim.getUniqueId());
		victimFaction = victimFactionPlayer.getClan();
		if (victimFaction == null) return;

		Player attacker = null;
		if (entityAttacker instanceof Player) {
			attacker = (Player) entityAttacker;
		} else if (entityAttacker instanceof Projectile) {
			// Projectile = arrow, all potions ...
			final ProjectileSource shooter = ((Projectile) entityAttacker).getShooter();
			if (shooter instanceof Player) attacker = (Player) shooter;
		}
		if (attacker == null) return;

		final FactionPlayer attackerFactionPlayer = AccountProvider.get(attacker.getUniqueId());
		final Faction attackerFaction = attackerFactionPlayer.getClan();
		if (attackerFaction == null) return;

		if (victimFaction.isSameClan(attackerFaction)) {
			Prefix.FACTION.sendMessage(attacker, "&cAttaque pas le collègue &4%s&c !", victim.getName());
			event.setCancelled(false);
		} else {
			final Chunk victimChunk = victim.getLocation().getChunk();
			final FactionClaim factionClaim = PvPFaction.getInstance().getClaimsManager().ofChunk(victimChunk);
			if (factionClaim == null) {
				Prefix.FACTION.sendMessage(attacker, "&4Impossible de charger le claim.");
				event.setCancelled(false);
				//Retiré car méthodes modifiées
			}/* else if (factionClaim.getPlayerPerm(attackerFactionPlayer).canInteract()) {
				Prefix.FACTION.sendMessage(attacker, "&cImpossible d'attaquer &4%s&c dans son claim.", victim.getName());
				event.setCancelled(false);
			}*/
		}
	}

	@EventHandler
	public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
		final Player player = event.getPlayer();
		final Material material = event.getBucket();
		if (material != Material.LAVA_BUCKET) return;

		final Location location = event.getBlockClicked().getLocation();
		final FactionPlayer attackerFactionPlayer = AccountProvider.get(player.getUniqueId());
		final Faction faction = attackerFactionPlayer.getClan();
		if (faction.getOnlineFactionPlayers().stream().anyMatch(p -> SpigotUtils.playerisIn(p.getPlayer(), location))) {
			Prefix.FACTION.sendMessage(player, "Brûle pas le collègue !");
			event.setCancelled(true);
			player.updateInventory();
		}
	}
}
