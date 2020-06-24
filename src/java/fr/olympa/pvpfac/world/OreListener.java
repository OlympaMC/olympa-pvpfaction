package fr.olympa.pvpfac.world;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import fr.olympa.pvpfac.PvPFaction;

public class OreListener implements Listener {
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		if (!event.isNewChunk())
			return;
		Chunk chunk = event.getChunk();
		PvPFaction.getInstance().getTask().runTaskAsynchronously(() -> {
			int i = 0;
			for (int iY = 0; 132 > iY; iY++)
				for (int iX = 0; 15 > iX; iX++)
					for (int iZ = 0; 16 > iZ; iZ++) {
						Block block = chunk.getBlock(iY, iX, iZ);
						if (block.getType().name().contains("_ORE")) {
							i++;
							PvPFaction.getInstance().getTask().runTask(() -> block.setType(Material.COBWEB));
						}
					}
			System.out.println("ORE " + i + " " + chunk.getX() + " " + chunk.getZ());
		});
	}
}
