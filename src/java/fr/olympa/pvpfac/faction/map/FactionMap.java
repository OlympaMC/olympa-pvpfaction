package fr.olympa.pvpfac.faction.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.faction.FactionManager;
import net.md_5.bungee.api.ChatColor;

public class FactionMap {

	private static List<String> symboles = Arrays.asList("#", "%", "&", "$", "@", "=", "+", "A", "B", "C", "D", "E", "G", "0", "7");
	protected static List<Player> autoMapPlayers = new ArrayList<>();

	public static void sendMap(Player player, Faction playerFaction) {
		player.sendMessage(getMap(player.getLocation(), playerFaction));
	}

	public static String getMap(Location location, Faction playerFaction) {
		Chunk chunk = location.getChunk();
		World world = chunk.getWorld();
		int chunkX = chunk.getX();
		int chunkZ = chunk.getZ();
		int mapRaduisSize = 7;
		int sidesCoeff = 2;
		int startX, startZ, endX, endZ;
		String facingName;
		BlockFace facing = SpigotUtils.yawToFace(location.getYaw(), false);
		FactionManager manager = PvPFaction.getInstance().getFactionManager();
		Map<Faction, String> factions = new HashMap<>();
		int indexSymbole = 0;
		StringJoiner sj = new StringJoiner("\n");

		sj.add("&e&m-------&6 MAP %facing %co &e&m-------&7".replace("%co", location.getBlockX() + " " + location.getBlockZ()));
		StringBuilder sb = new StringBuilder();
		switch (facing) {
		default:
		case NORTH:
			facingName = "Nord";
			startX = chunkX - mapRaduisSize * sidesCoeff;
			endX = chunkX + mapRaduisSize * sidesCoeff;
			startZ = chunkZ - mapRaduisSize;
			endZ = chunkZ + mapRaduisSize;
			for (int iZ = startZ; endZ > iZ; iZ++) {
				for (int iX = startX; endX > iX; iX++)
					sb.append(getChunkLetter(manager.getByChunk(world.getChunkAt(iX, iZ)), factions, indexSymbole, playerFaction));
				sb.append("\n");
			}
			break;
		case EAST:
			facingName = "Est";
			startX = chunkX + mapRaduisSize;
			endX = chunkX - mapRaduisSize;
			startZ = chunkZ - mapRaduisSize * sidesCoeff;
			endZ = chunkZ + mapRaduisSize * sidesCoeff;
			for (int iX = startX; endX <= iX; iX--) {
				for (int iZ = startZ; endZ > iZ; iZ++)
					sb.append(getChunkLetter(manager.getByChunk(world.getChunkAt(iX, iZ)), factions, indexSymbole, playerFaction));
				sb.append("\n");
			}
			break;
		case WEST:
			facingName = "Ouest";
			startX = chunkX - mapRaduisSize;
			endX = chunkX + mapRaduisSize;
			startZ = chunkZ + mapRaduisSize * sidesCoeff;
			endZ = chunkZ - mapRaduisSize * sidesCoeff;
			for (int iX = startX; endX > iX; iX++) {
				for (int iZ = startZ; endZ <= iZ; iZ--)
					sb.append(getChunkLetter(manager.getByChunk(world.getChunkAt(iX, iZ)), factions, indexSymbole, playerFaction));
				sb.append("\n");
			}
			break;
		case SOUTH:
			facingName = "Sud";
			startX = chunkX + mapRaduisSize * sidesCoeff;
			endX = chunkX - mapRaduisSize * sidesCoeff;
			startZ = chunkZ + mapRaduisSize;
			endZ = chunkZ - mapRaduisSize;
			for (int iZ = startZ; endZ <= iZ; iZ--) {
				for (int iX = startX; endX <= iX; iX--)
					sb.append(getChunkLetter(manager.getByChunk(world.getChunkAt(iX, iZ)), factions, indexSymbole, playerFaction));
				sb.append("\n");
			}
			break;
		}
		int maxX = Math.abs(startX - endX);
		int maxZ = Math.abs(startZ - endZ);
		System.out.println("MAP " + facingName + " sizeX " + maxX + " sizeZ " + maxZ);
		sj.add(sb.toString());
		if (!factions.isEmpty())
			sj.add(factions.entrySet().stream().map(entry -> "&7" + entry.getValue() + "&a = &7" + entry.getKey().getName()).collect(Collectors.joining("&7, ")));
		sj.add("&6TIPS &aFaites F3 + G pour voir la taille des claims.");
		return ColorUtils.color(sj.toString().replace("%facing", facingName));
	}

	public static String getChunkLetter(Faction fChunk, Map<Faction, String> factions, int indexSymbole, Faction faction) {
		if (fChunk == null)
			return "&7-";
		String symb = factions.get(fChunk);
		if (symb == null) {
			symb = symboles.get(indexSymbole++);
			factions.put(fChunk, symb);
		}
		ChatColor color = ChatColor.RED;
		if (faction != null && faction.getID() == fChunk.getID())
			color = ChatColor.GREEN;
		return color + symb;
	}

	public static void addAutoMap(Player player) {
		autoMapPlayers.add(player);
	}
}
