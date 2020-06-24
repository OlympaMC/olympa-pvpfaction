package fr.olympa.pvpfac.world;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class OreListener implements Listener {

	private static List<Material> ores = Arrays.asList(Material.DIAMOND_ORE, Material.GOLD_ORE, Material.IRON_ORE, Material.EMERALD_ORE, Material.LAPIS_ORE, Material.COAL_ORE);
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		if (!event.isNewChunk())
			return;
		Chunk chunk = event.getChunk();
		for (int iY = 0; 256 > iY; iY++)
			for (int iX = 0; 16 > iX; iX++)
				for (int iZ = 0; 16 > iZ; iZ++) {
					Block block = chunk.getBlock(iY, iX, iZ);
					if (block.getType() == Material.STONE)
						block.setType(Material.AIR);
					else if (ores.contains(block.getType()))
						block.setType(Material.COBWEB);
				}
	}
}
