package fr.olympa.pvpfac.world;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class OreListener implements Listener {
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		if (!event.isNewChunk())
			return;
		Chunk chunk = event.getChunk();
		for (int iY = 0; 132 > iY; iY++)
			for (int iX = 0; 16 > iX; iX++)
				for (int iZ = 0; 16 > iZ; iZ++) {
					Block block = chunk.getBlock(iY, iX, iZ);
					if (block.getType() == Material.GRASS)
						block.setType(Material.AIR);
					else if (block.getType().name().contains("_ORE"))
						block.setType(Material.COBWEB);
				}
	}
}
