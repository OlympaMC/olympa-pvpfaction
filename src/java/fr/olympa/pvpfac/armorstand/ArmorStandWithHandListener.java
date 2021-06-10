package fr.olympa.pvpfac.armorstand;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class ArmorStandWithHandListener implements Listener {
	@EventHandler
	public void onCreatureSpawn(final CreatureSpawnEvent event) {
		if (event.isCancelled() || event.getEntityType() != EntityType.ARMOR_STAND) return;
		final ArmorStand armorStand = (ArmorStand) event.getEntity();
		armorStand.setArms(true);
	}
}
