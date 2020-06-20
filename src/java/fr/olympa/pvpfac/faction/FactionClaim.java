package fr.olympa.pvpfac.faction;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class FactionClaim {

	String worldName;
	int x;
	int z;
	
	public FactionClaim(Chunk chunk) {
		worldName = chunk.getWorld().getName();
		if (worldName.equals("world"))
			worldName = null;
		x = chunk.getX();
		z = chunk.getZ();
	}

	public World getWorld() {
		return worldName == null ? Bukkit.getWorlds().get(0) : Bukkit.getWorld(worldName);
	}

	public String getWorldName() {
		return worldName == null ? Bukkit.getWorlds().get(0).getName() : worldName;
	}
	
	public boolean isDefaultWorld() {
		return worldName == null;
	}
	
	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}
	
	public boolean isChunk(Chunk chunk) {
		return x == chunk.getX() && z == chunk.getZ() && chunk.getWorld().getName().equals(getWorldName());
	}
}
