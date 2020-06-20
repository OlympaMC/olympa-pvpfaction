package fr.olympa.pvpfac.factionold;

//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.bukkit.Chunk;
//import org.bukkit.entity.Player;
//
//import fr.olympa.api.utils.ColorUtils;
//import fr.olympa.api.utils.Prefix;
//import fr.olympa.pvpfac.PvPFaction;

public class FactionHandler {
	
	//	private static List<OlympaFaction> cacheFactions = new ArrayList<>();
	//	private static Map<OlympaFactionInvite, Player> invites = new HashMap<>();
	//	private static List<Player> tryCreate = new ArrayList<>();
	//
	//	public static void addFaction(OlympaFaction faction) {
	//		FactionHandler.cacheFactions.add(faction);
	//	}
	//
	//	public static void addInvite(Player player, Player author, OlympaFaction faction) {
	//		OlympaFactionInvite invite = new OlympaFactionInvite(author, faction);
	//		FactionHandler.invites.put(invite, player);
	//		PvPFaction.getInstance().getTask().runTaskLater(() -> {
	//			Set<OlympaFactionInvite> invitePlayer = getInvites(player);
	//			if (invitePlayer.contains(invite)) {
	//				invites.remove(invite);
	//				for (Player members : faction.getOnlinePlayers()) {
	//					members.sendMessage(ColorUtils.color(Prefix.FACTION + "&cL'invitation pour &4" + author.getName() + "&c a expirée."));
	//				}
	//				player.sendMessage(ColorUtils.color(Prefix.FACTION + "&cL'invitation pour rejoindre &4" + faction.getName() + "&c a expirée."));
	//			}
	//		}, 60 * 20);
	//	}
	//
	//	public static void addPlayerTryingToCreateFaction(Player player) {
	//		FactionHandler.tryCreate.add(player);
	//	}
	//
	//	public static OlympaFaction getCacheFaction(Player player) {
	//		return cacheFactions.stream().filter(f -> f.getMembers().containsKey(player.getUniqueId())).findFirst().orElse(null);
	//	}
	//
	//	public static OlympaFaction getFaction(Chunk chunk) {
	//		return cacheFactions.stream().filter(f -> f.getChunks().contains(chunk)).findFirst().orElse(null);
	//	}
	//
	//	public static OlympaFaction getFaction(int id) {
	//		return cacheFactions.stream().filter(f -> f.getId() == id).findFirst().orElse(null);
	//	}
	//
	//	public static OlympaFaction getFaction(String name) {
	//		OlympaFaction faction = getFactionByName(name);
	//		if (faction == null) {
	//			faction = getFactionByTag(name);
	//			if (faction == null) {
	//				faction = getFactionByOnlinePlayer(name);
	//				if (faction == null) {
	//					faction = getFactionByOfflinePlayer(name);
	//				}
	//			}
	//		}
	//		return faction;
	//	}
	//
	//	public static OlympaFaction getFactionByName(String name) {
	//		return cacheFactions.stream().filter(f -> f.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	//	}
	//
	//	public static OlympaFaction getFactionByOfflinePlayer(String name) {
	//		return cacheFactions.stream().filter(f -> f.getOfflinePlayer().stream().anyMatch(p -> p.getName().equalsIgnoreCase(name))).findFirst().orElse(null);
	//	}
	//
	//	public static OlympaFaction getFactionByOnlinePlayer(String name) {
	//		return cacheFactions.stream().filter(f -> f.getOnlinePlayers().stream().anyMatch(p -> p.getName().equalsIgnoreCase(name))).findFirst().orElse(null);
	//	}
	//
	//	public static OlympaFaction getFactionByTag(String tag) {
	//		return cacheFactions.stream().filter(f -> f.getTag().equalsIgnoreCase(tag)).findFirst().orElse(null);
	//	}
	//
	//	public static OlympaFactionInvite getInvite(Player player, String name) {
	//		Set<OlympaFactionInvite> invitesPlayer = getInvites(player);
	//		OlympaFactionInvite faction = getInviteByFactionName(invitesPlayer, name);
	//		if (faction == null) {
	//			faction = getInviteByFactionTag(invitesPlayer, name);
	//			if (faction == null) {
	//				faction = getInviteByOnlinePlayerName(invitesPlayer, name);
	//				if (faction == null) {
	//					faction = getInviteByOfflinePlayerName(invitesPlayer, name);
	//				}
	//			}
	//		}
	//		return faction;
	//	}
	//
	//	public static OlympaFactionInvite getInviteByFaction(Player player, OlympaFaction faction) {
	//		return getInvites(player).stream().filter(invite -> invite.getFaction().getId() == faction.getId()).findFirst().orElse(null);
	//	}
	//
	//	public static OlympaFactionInvite getInviteByFactionName(Set<OlympaFactionInvite> invitesPlayer, String name) {
	//		return invitesPlayer.stream().filter(invite -> invite.getFaction().getName().equals(name)).findFirst().orElse(null);
	//	}
	//
	//	public static OlympaFactionInvite getInviteByFactionTag(Set<OlympaFactionInvite> invitesPlayer, String tag) {
	//		return invitesPlayer.stream().filter(invite -> invite.getFaction().getTag().equals(tag)).findFirst().orElse(null);
	//	}
	//
	//	public static OlympaFactionInvite getInviteByOfflinePlayerName(Set<OlympaFactionInvite> invitesPlayer, String name) {
	//		return invitesPlayer.stream().filter(invite -> invite.getFaction().getOfflinePlayer().stream().anyMatch(p -> p.getName().equalsIgnoreCase(name))).findFirst()
	//				.orElse(null);
	//	}
	//
	//	public static OlympaFactionInvite getInviteByOnlinePlayerName(Set<OlympaFactionInvite> invitesPlayer, String name) {
	//		return invitesPlayer.stream().filter(invite -> invite.getFaction().getOnlinePlayers().stream().anyMatch(p -> p.getName().equalsIgnoreCase(name))).findFirst()
	//				.orElse(null);
	//	}
	//
	//	public static Set<OlympaFactionInvite> getInvites(Player player) {
	//		return FactionHandler.invites.entrySet().stream().filter(entry -> entry.getValue().getUniqueId().equals(player.getUniqueId())).map(entry -> entry.getKey()).collect(Collectors.toSet());
	//	}
	//
	//	public static boolean isPlayerTryingToCreateFaction(Player player) {
	//		return tryCreate.contains(player);
	//	}
	//
	//	public static void removeFaction(OlympaFaction faction) {
	//		FactionHandler.cacheFactions.remove(faction);
	//	}
	//
	//	public static void removeInvite(OlympaFactionInvite invite) {
	//		invites.remove(invite);
	//	}
	//
	//	public static void removePlayerTryingToCreateFaction(Player player) {
	//		FactionHandler.tryCreate.remove(player);
	//	}
}
