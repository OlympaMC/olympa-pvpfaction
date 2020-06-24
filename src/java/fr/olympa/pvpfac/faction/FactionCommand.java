package fr.olympa.pvpfac.faction;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClansCommand;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.PvPFactionPermission;
import fr.olympa.pvpfac.faction.chat.FactionChat;
import fr.olympa.pvpfac.faction.utils.FactionMsg;
import fr.olympa.pvpfac.faction.utils.FactionUtils;
import fr.olympa.pvpfac.player.FactionPlayer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class FactionCommand<T extends Clan<Faction>> extends ClansCommand<Faction> {
	
	public FactionCommand(FactionManager manager, String name, String description, OlympaPermission permission, String... aliases) {
		super(manager, name, description, permission, aliases);
	}

	@Cmd(player = true, aliases = { "setdesc", "adddesc", "setdescription", "adddescription" }, args = { "100 lettres max" }, min = 1)
	public void description(CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'infos.");
			return;
		}
		//		if (!OlympaFactionRole.OFFICER.hasPermission(faction.getRole(player))) {
		//			Set<FactionPlayer> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
		//			StringBuilder sb = new StringBuilder();
		//			if (!can.isEmpty())
		//				sb.append(" Demande à &4" + can.stream().map(FactionPlayer::getName).collect(Collectors.joining("&c, &4")) + "&c.");
		//			sendMessage(Prefix.FACTION, "&cTu n'a pas la permission." + sb.toString());
		//			return;
		//		}
		try {
			faction.updateDescription(Utils.capitalize(cmd.getFrom(1).toString().replace("\n", "")));
			Prefix.FACTION.sendMessage(faction.getPlayers(), "&2%s&a a changer la decription en &2%s&a.", player.getName(), faction.getDescription());
		} catch (SQLException e) {
			e.printStackTrace();
			sendError();
		}
	}

	@Cmd(player = true, aliases = { "settag", "addtag" }, args = { "6 lettres max" }, min = 1)
	public void tag(CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'infos.");
			return;
		}
		//		if (!OlympaFactionRole.OFFICER.hasPermission(faction.getRole(player))) {
		//			Set<FactionPlayer> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
		//			StringBuilder sb = new StringBuilder();
		//			if (!can.isEmpty())
		//				sb.append(" Demande à &4" + can.stream().map(FactionPlayer::getName).collect(Collectors.joining("&c, &4")) + "&c.");
		//			sendMessage(Prefix.FACTION, "&cTu n'a pas la permission." + sb.toString());
		//			return;
		//		}
		try {
			faction.updateTag(cmd.getArgument(0).toString().toUpperCase());
			Prefix.FACTION.sendMessage(faction.getPlayers(), "&2%s&a a changer le tag en &2%s&a.", player.getName(), faction.getTag());
		} catch (SQLException e) {
			e.printStackTrace();
			sendError();
		}
	}
	
	@Cmd(player = true, aliases = { "p", "powers" }, args = { "PLAYERS" })
	public void power(CommandContext cmd) {
		FactionPlayer fp;
		if (cmd.getArgumentsLength() != 0)
			try {
				fp = AccountProvider.get(cmd.getArgument(0, new String()));
				if (cmd.getArgumentsLength() != 1 && PvPFactionPermission.FACTION_BYPASS.hasSenderPermission(player) && Matcher.isInt(cmd.getArgument(1))) {
					fp.setPower(cmd.getArgument(1));
					sendMessage(Prefix.FACTION, "&aLe power de &2" + fp.getName() + "&a est désormais de &2" + fp.getPower() + "&a/" + FactionPlayer.POWER_MAX + ".");
					return;
				}
			} catch (SQLException e) {
				sendError();
				e.printStackTrace();
				return;
			}
		else
			fp = getOlympaPlayer();
		sendMessage(Prefix.FACTION, "&2" + fp.getName() + "&a a &2" + fp.getPower() + "&a/" + FactionPlayer.POWER_MAX + " de power.");
	}

	@Cmd(player = true, aliases = "cl")
	public void claim(CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (cmd.getArgumentsLength() > 0 && PvPFactionPermission.FACTION_BYPASS.hasSenderPermission(player))
			try {
				faction = PvPFaction.getInstance().getFactionManager().get(cmd.getArgument(0));
			} catch (SQLException e) {
				e.printStackTrace();
				sendError();
				return;
			}
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'infos.");
			return;
		}
		//		if (!OlympaFactionRole.OFFICER.hasPermission(faction.getRole(player))) {
		//			Set<FactionPlayer> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
		//			StringBuilder sb = new StringBuilder();
		//			if (!can.isEmpty())
		//				sb.append(" Demande à &4" + can.stream().map(FactionPlayer::getName).collect(Collectors.joining("&c, &4")) + "&c.");
		//			sendMessage(Prefix.FACTION, "&cTu n'a pas la permission." + sb.toString());
		//			return;
		//		}
		Chunk chunk = player.getLocation().getChunk();
		if (faction.hasClaim(chunk)) {
			sendMessage(Prefix.FACTION, "&cCe claim appartient déjà à ta faction.");
			return;
		}
		Set<Entry<Integer, Faction>> clans = PvPFaction.getInstance().getFactionManager().getClans();
		Faction fChunk = clans.stream().filter(c -> c.getValue().hasClaim(chunk)).map(e -> e.getValue()).findFirst().orElse(null);

		try {
			if (fChunk != null) {
				if (!fChunk.isOverClaimable()) {
					TextComponent text = new TextComponent(TextComponent.fromLegacyText(ColorUtils.color("&cImpossible de &lsur&cclaim le chunk de la faction &4%s&c.".replace("%s", fChunk.getName()))));
					text.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText(ColorUtils.color("&6&lTIPS &ePour surclaim une faction, celle-ci doit avoir moins de power que de claim."))));
					sendComponents(text);
					return;
				}
				fChunk.unclaim(chunk);
				faction.claim(chunk);
				sendMessage(faction.getPlayers(), Prefix.FACTION, "&2" + player.getName() + "&a a &lsur&aclaim un chunk de &c" + fChunk.getName() + "&a.");
				return;
			}
			faction.claim(chunk);
			sendMessage(faction.getPlayers(), Prefix.FACTION, "&2" + player.getName() + "&a a claim un chunk.");
		} catch (SQLException e) {
			e.printStackTrace();
			sendError();
		}
	}

	@Cmd(player = true, aliases = "ucl")
	public void unclaim(CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (cmd.getArgumentsLength() > 0 && PvPFactionPermission.FACTION_BYPASS.hasSenderPermission(player))
			try {
				faction = PvPFaction.getInstance().getFactionManager().get(cmd.getArgument(0));
			} catch (SQLException e) {
				e.printStackTrace();
				sendError();
				return;
			}
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'infos.");
			return;
		}
		//		if (!OlympaFactionRole.OFFICER.hasPermission(faction.getRole(player))) {
		//			Set<FactionPlayer> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
		//			StringBuilder sb = new StringBuilder();
		//			if (!can.isEmpty())
		//				sb.append(" Demande à &4" + can.stream().map(FactionPlayer::getName).collect(Collectors.joining("&c, &4")) + "&c.");
		//			sendMessage(Prefix.FACTION, "&cTu n'a pas la permission." + sb.toString());
		//			return;
		//		}
		Chunk chunk = player.getLocation().getChunk();
		if (!faction.hasClaim(chunk)) {
			sendMessage(Prefix.FACTION, "&cCe claim n'appartient pas à ta faction.");
			return;
		}
		try {
			faction.unclaim(chunk);
			sendMessage(faction.getPlayers(), Prefix.FACTION, "&2%s&a a &lun&aclaim un chunk.", player.getName());
		} catch (SQLException e) {
			e.printStackTrace();
			sendError();
		}
	}

	@Cmd(player = true, aliases = "m")
	public void map(CommandContext cmd) {
		Chunk chunk = player.getLocation().getChunk();
		World world = chunk.getWorld();
		int chunkX = chunk.getX();
		int chunkZ = chunk.getZ();
		int mapRaduisSize = 7;
		int sidesCoeff = 2;
		int startX, startZ, endX, endZ;
		String facingName;
		BlockFace facing = SpigotUtils.yawToFace(player.getLocation().getYaw(), false);
		FactionManager manager = PvPFaction.getInstance().getFactionManager();
		Map<Faction, String> factions = new HashMap<>();
		int indexSymbole = 0;
		StringJoiner sj = new StringJoiner("\n");
		sj.add("&e&m--------------&6 MAP %facing &e&m--------------&7");
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
					sb.append(FactionUtils.getChunkLetter(manager.getByChunk(world.getChunkAt(iX, iZ)), factions, indexSymbole, getPlayerClan(false)));
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
					sb.append(FactionUtils.getChunkLetter(manager.getByChunk(world.getChunkAt(iX, iZ)), factions, indexSymbole, getPlayerClan(false)));
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
					sb.append(FactionUtils.getChunkLetter(manager.getByChunk(world.getChunkAt(iX, iZ)), factions, indexSymbole, getPlayerClan(false)));
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
					sb.append(FactionUtils.getChunkLetter(manager.getByChunk(world.getChunkAt(iX, iZ)), factions, indexSymbole, getPlayerClan(false)));
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
		player.sendMessage(ColorUtils.color(sj.toString().replace("%facing", facingName)));
	}
	
	@Cmd(player = true, aliases = "c", args = { "Géneral", "Faction", "Allié" })
	public void chat(CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'infos.");
			return;
		}
		
		FactionPlayer player = getOlympaPlayer();
		FactionChat askChat;
		FactionChat chat = player.getChat();
		if (cmd.getArgumentsLength() > 0) {
			askChat = FactionChat.get(cmd.getArgument(0));
			if (askChat == null) {
				sendMessage(Prefix.FACTION, "&cLe chat &4" + cmd.getArgument(1) + "&c n'existe pas.");
				return;
			} else if (chat != null && chat.equals(askChat)) {
				sendMessage(Prefix.FACTION, "&cTu utilise déjà le chat &4" + chat.getName() + "&c.");
				return;
			}
			player.setChat(askChat);
		} else
			askChat = chat.getOther();
		player.setChat(askChat);
		sendMessage(Prefix.FACTION, "Tu parle désormais en chat &2" + askChat.getName() + "&a.");
	}

	@Cmd(player = true, aliases = { "who", "f" })
	public void show(CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (cmd.getArgumentsLength() == 0) {
			if (FactionMsg.youHaveNoFaction(player, faction)) {
				sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f show <nom|tag|joueur>&c pour plus d'infos.");
				return;
			}
		} else {
			FactionManager manager = PvPFaction.getInstance().getFactionManager();
			try {
				faction = manager.get(cmd.getArgument(1));
			} catch (SQLException e) {
				e.printStackTrace();
				sendMessage(Prefix.FACTION, "&cUne erreur est survenu.");
				return;
			}
			if (faction == null) {
				sendMessage(Prefix.FACTION, "&cNom, tag ou joueur &4" + cmd.getArgument(1) + "&c inconnu.");
				return;
			}
		}
		Faction playerFaction = ((FactionPlayer) getOlympaPlayer()).getClan();
		ChatColor color = playerFaction != null && faction.getID() == playerFaction.getID() ? ChatColor.GREEN : ChatColor.RED;
		StringJoiner sj = new StringJoiner("\n");
		sj.add("&e&m--------------&6 " + color + faction.getName() + " &e&m--------------&6");
		String tag = faction.getTag();
		if (tag != null && !tag.isBlank())
			sj.add("&3Tag: &b" + tag);
		String description = faction.getDescription();
		if (description != null && !description.isBlank())
			sj.add("&3Description: &b" + description);
		long creationTime = faction.getCreationTime();
		sj.add("&3Crée le : &b" + Utils.timestampToDate(creationTime) + "&3 (" + Utils.timestampToDuration(creationTime) + ")");
		sj.add("&3Claims/Power/MaxPower : &b" + faction.getClaimsPowerMaxPower());
		sj.add("&3Joueurs max: &b" + faction.getMaxSize());
		sj.add("&3Joueurs connectés: &a" + faction.getOnlineFactionPlayers().stream().map(p -> p.getName()).collect(Collectors.joining("&b, &a")));
		sj.add("&3Joueurs déconnectés: &c" + faction.getOfflineFactionPlayers().stream().map(p -> p.getName()).collect(Collectors.joining("&b, &c")));
		player.sendMessage(ColorUtils.color(sj.toString()));
	}
}
