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

	public static void sendMap(final Player player, final Faction playerFaction) {
		player.sendMessage(getMap(player.getLocation(), playerFaction));
	}

	public static String getMap(final Location location, final Faction playerFaction) {
		final Chunk chunk = location.getChunk();
		final World world = chunk.getWorld();
		final int chunkX = chunk.getX();
		final int chunkZ = chunk.getZ();
		final int mapRadiusSize = 4;
		final int sidesCoeff = 2;
		final int startX;
		final int startZ;
		final int endX;
		final int endZ;
		final String facingName;
		final BlockFace facing = SpigotUtils.yawToFace(location.getYaw(), false);
		final FactionClaimsManager manager = PvPFaction.getInstance().getClaimsManager();
		final Map<Faction, String> factions = new HashMap<>();
		int indexSymbole = 0;
		final StringJoiner sj = new StringJoiner("\n");

		sj.add("&6MAP %s %co".replace("%co", location.getBlockX() + " " + location.getBlockZ()));
		final StringBuilder sb = new StringBuilder();
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

	public static String getChunkLetter(final FactionClaim factionClaim, final Map<Faction, String> factions, final int indexSymbole, final Faction targetFaction, final boolean isCenter) {
		final Faction claimFaction = factionClaim.getFaction();
		if (claimFaction == null) {
			return /*factionClaim.getColor() + */(isCenter ? "§m" : "") + "-";
		}
		final String symbol = factions.computeIfAbsent(claimFaction, k -> SYMBOLES.get(indexSymbole));
		ChatColor color = ChatColor.RED;
		if (targetFaction != null && targetFaction.getID() == claimFaction.getID()) {
			color = ChatColor.GREEN;
		}
		return color + (isCenter ? "§m" : "") + symbol;
	}

	public static void toggleAutoMap(final Player player) {
		if (autoMapPlayers.contains(player)) {
			autoMapPlayers.remove(player);
			Prefix.FACTION.sendMessage(player, "&cAutoMap désactivée.");
		} else {
			autoMapPlayers.add(player);
			Prefix.FACTION.sendMessage(player, "AutoMap activée.");
		}
	}
}
