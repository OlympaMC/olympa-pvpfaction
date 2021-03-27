package fr.olympa.pvpfac.armorstand;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class ArmorStandWithHandListener implements Listener {
	@EventHandler(ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.getEntityType() != EntityType.ARMOR_STAND)
			return;
		ArmorStand armorStand = (ArmorStand) event.getEntity();
		armorStand.setArms(true);
	}
}
