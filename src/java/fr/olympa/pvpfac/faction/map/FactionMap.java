package fr.olympa.pvpfac.faction.map;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.faction.claim.FactionClaim;
import fr.olympa.pvpfac.faction.claim.FactionClaimsManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class FactionMap {

	private static final List<String> SYMBOLES = Arrays.asList("#", "%", "&", "$", "@", "=", "+", "A", "B", "C", "D", "E", "G", "0", "7");
	protected static List<Player> autoMapPlayers = new ArrayList<>();

	public static void sendMap(Player player, Faction playerFaction) {
		player.sendMessage(getMap(player.getLocation(), playerFaction));
	}

	public static String getMap(Location location, Faction playerFaction) {
		Chunk chunk = location.getChunk();
		World world = chunk.getWorld();
		int chunkX = chunk.getX();
		int chunkZ = chunk.getZ();
		int mapRadiusSize = 4;
		int sidesCoeff = 2;
		int startX, startZ, endX, endZ;
		String facingName;
		BlockFace facing = SpigotUtils.yawToFace(location.getYaw(), false);
		FactionClaimsManager manager = PvPFaction.getInstance().getClaimsManager();
		Map<Faction, String> factions = new HashMap<>();
		int indexSymbole = 0;
		StringJoiner sj = new StringJoiner("\n");

		sj.add("&6MAP %s %co".replace("%co", location.getBlockX() + " " + location.getBlockZ()));
		StringBuilder sb = new StringBuilder();
		switch (facing) {
			default:
			case NORTH:
				facingName = "Nord";
				startX = chunkX - mapRadiusSize * sidesCoeff;
				endX = chunkX + mapRadiusSize * sidesCoeff;
				startZ = chunkZ - mapRadiusSize;
				endZ = chunkZ + mapRadiusSize;
				for (int iZ = startZ; endZ > iZ; iZ++) {
					for (int iX = startX; endX > iX; iX++) {
						sb.append(getChunkLetter(manager.ofChunk(world.getChunkAt(iX, iZ)), factions, indexSymbole++, playerFaction, iZ == chunkZ && iX == chunkX));
					}
					sb.append("\n");
				}
				break;
			case EAST:
				facingName = "Est";
				startX = chunkX + mapRadiusSize;
				endX = chunkX - mapRadiusSize;
				startZ = chunkZ - mapRadiusSize * sidesCoeff;
				endZ = chunkZ + mapRadiusSize * sidesCoeff;
				for (int iX = startX; endX < iX; iX--) {
					for (int iZ = startZ; endZ > iZ; iZ++) {
						sb.append(getChunkLetter(manager.ofChunk(world.getChunkAt(iX, iZ)), factions, indexSymbole++, playerFaction, iZ == chunkZ && iX == chunkX));
					}
					sb.append("\n");
				}
				break;
			case WEST:
				facingName = "Ouest";
				startX = chunkX - mapRadiusSize;
				endX = chunkX + mapRadiusSize;
				startZ = chunkZ + mapRadiusSize * sidesCoeff;
				endZ = chunkZ - mapRadiusSize * sidesCoeff;
				for (int iX = startX; endX > iX; iX++) {
					for (int iZ = startZ; endZ < iZ; iZ--) {
						sb.append(getChunkLetter(manager.ofChunk(world.getChunkAt(iX, iZ)), factions, indexSymbole++, playerFaction, iZ == chunkZ && iX == chunkX));
					}
					sb.append("\n");
				}
				break;
			case SOUTH:
				facingName = "Sud";
				startX = chunkX + mapRadiusSize * sidesCoeff;
				endX = chunkX - mapRadiusSize * sidesCoeff;
				startZ = chunkZ + mapRadiusSize;
				endZ = chunkZ - mapRadiusSize;
				for (int iZ = startZ; endZ < iZ; iZ--) {
					for (int iX = startX; endX < iX; iX--) {
						sb.append(getChunkLetter(manager.ofChunk(world.getChunkAt(iX, iZ)), factions, indexSymbole++, playerFaction, iZ == chunkZ && iX == chunkX));
					}
					sb.append("\n");
				}
				break;
		}
		sj.add(sb.toString());
		if (!factions.isEmpty()) {
			sj.add(factions.entrySet().stream().map(entry -> "&7" + entry.getValue() + "&a = &7" + entry.getKey().getName()).collect(Collectors.joining("&7, ")));
		}
		sj.add("&6TIPS &aFais F3 + G pour voir la taille des claims.");
		return ColorUtils.color(String.format(sj.toString(), facingName));
	}

	public static String getChunkLetter(FactionClaim factionClaim, Map<Faction, String> factions, int indexSymbole, Faction targetFaction, boolean isCenter) {
		Faction claimFaction = factionClaim.getFaction();
		if (claimFaction == null) {
			return /*factionClaim.getColor() + */(isCenter ? "§m" : "") + "-";
		}
		String symbol = factions.computeIfAbsent(claimFaction, k -> SYMBOLES.get(indexSymbole));
		ChatColor color = ChatColor.RED;
		if (targetFaction != null && targetFaction.getID() == claimFaction.getID()) {
			color = ChatColor.GREEN;
		}
		return color + (isCenter ? "§m" : "") + symbol;
	}

	public static void toggleAutoMap(Player player) {
		if (!autoMapPlayers.contains(player)) {
			autoMapPlayers.add(player);
			Prefix.FACTION.sendMessage(player, "AutoMap activée.");
		} else {
			autoMapPlayers.remove(player);
			Prefix.FACTION.sendMessage(player, "&cAutoMap désactivée.");
		}
	}
}
