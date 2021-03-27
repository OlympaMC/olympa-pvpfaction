package fr.olympa.pvpfac.world;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkPopulateEvent;

import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.world.WorldManager.WorldType;

public class WorldsListener implements Listener {
	
	@EventHandler
	public void onChunkPopulate(ChunkPopulateEvent event) {
		if (!event.getWorld().getName().equals("world")) return;
		Chunk chunk = event.getChunk();
		long time = System.currentTimeMillis();
		//PvPFaction.getInstance().getTask().runTaskAsynchronously(() -> {
			int i = 0;
			for (int iY = 0; 132 > iY; iY++)
				for (int iX = 0; 16 > iX; iX++)
					for (int iZ = 0; 16 > iZ; iZ++) {
						Block block = chunk.getBlock(iX, iY, iZ);
						
						if (block.getType() == Material.GOLD_ORE || block.getType() == Material.DIAMOND_ORE || block.getType() == Material.EMERALD_ORE) {
							i++;
							//PvPFaction.getInstance().getTask().runTask(() -> block.setType(Material.COBWEB));
							block.setType(Material.COBWEB);
						}
					}
			System.out.println(i + " ores in chunk X: " + chunk.getX() + " Z: " + chunk.getZ() + " (" + (System.currentTimeMillis() - time) + "ms)");
			//});
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		e.setRespawnLocation(WorldType.OVERWORLD.getWorld().getSpawnLocation());
	}
	
	@EventHandler
	public void onPortalEnterEntity(EntityPortalEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onPortalEnterPlayer(PlayerPortalEvent e) {
		e.setCancelled(true);
	}
}






