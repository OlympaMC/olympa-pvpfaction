package fr.olympa.pvpfac.faction;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import fr.olympa.api.clans.OlympaFactionRole;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.player.FactionPlayer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class FactionCommand<T extends Clan<Faction>> extends ClansCommand<Faction> {

	public FactionCommand(FactionManager manager, String name, String description, OlympaPermission permission, String... aliases) {
		super(manager, name, description, permission, aliases);
	}

	@Cmd(player = true)
	public void claim(CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'infos.");
			return;
		}
		if (!OlympaFactionRole.OFFICER.hasPermission(faction.getRole(player))) {
			Set<FactionPlayer> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
			StringBuilder sb = new StringBuilder();
			if (!can.isEmpty())
				sb.append(" Demande à &4" + can.stream().map(FactionPlayer::getName).collect(Collectors.joining("&c, &4")) + "&c.");
			sendMessage(Prefix.FACTION, "&cTu n'a pas la permission." + sb.toString());
			return;
		}
		Chunk chunk = player.getLocation().getChunk();
		if (faction.hasClaim(chunk)) {
			sendMessage(Prefix.FACTION, "&cCe claim appartient déjà à ta faction.");
			return;
		}
		Set<Entry<Integer, Faction>> clans = PvPFaction.getInstance().getFactionManager().getClans();
		Faction fChunk = clans.stream().filter(c -> c.getValue().hasClaim(chunk)).map(e -> e.getValue()).findFirst().orElse(null);

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
	}

	private static List<String> symboles = Arrays.asList("#", "%", "&", "$", "@", "=", "+", "A", "B", "C", "D", "E", "G", "0", "7");

	@Cmd(player = true)
	public void map(CommandContext cmd) {
		Chunk chunk = player.getLocation().getChunk();
		World world = chunk.getWorld();
		int chunkX = chunk.getX();
		int chunkZ = chunk.getZ();
		int mapRaduisSize = 5;
		int startX, startZ, endX, endZ;
		String facingName;
		BlockFace facing = SpigotUtils.yawToFace(player.getLocation().getYaw(), false);
		switch (facing) {
		default:
		case NORTH:
			facingName = "Nord";
			startX = chunkX - mapRaduisSize;
			endX = chunkX + mapRaduisSize;
			startZ = chunkZ - mapRaduisSize;
			endZ = chunkZ + mapRaduisSize;
			break;
		case EAST:
			facingName = "Est";
			startX = chunkX + mapRaduisSize;
			endX = chunkX - mapRaduisSize;
			startZ = chunkZ - mapRaduisSize;
			endZ = chunkZ + mapRaduisSize;
			break;
		case WEST:
			facingName = "Ouest";
			startX = chunkX - mapRaduisSize;
			endX = chunkX + mapRaduisSize;
			startZ = chunkZ + mapRaduisSize;
			endZ = chunkZ - mapRaduisSize;
			break;
		case SOUTH:
			facingName = "Sud";
			startX = chunkX + mapRaduisSize;
			endX = chunkX - mapRaduisSize;
			startZ = chunkZ + mapRaduisSize;
			endZ = chunkZ - mapRaduisSize;
			break;
		}
		FactionManager manager = PvPFaction.getInstance().getFactionManager();
		Map<Faction, String> factions = new HashMap<>();
		StringJoiner sj = new StringJoiner("\n");
		sj.add("&e&m----- &6MAP " + facingName + " &e&m-----");
		StringJoiner sj2 = new StringJoiner(" ");
		int indexSymbole = 0;
		for (int iX = startX; endX > iX; iX++) {
			for (int iZ = startZ; endZ > iZ; iZ++) {
				Chunk targetChunk = world.getChunkAt(iX, iZ);
				Faction fChunk = manager.getByChunk(targetChunk);
				if (fChunk != null) {
					String symb = factions.get(fChunk);
					if (symb == null) {
						symb = symboles.get(indexSymbole++);
						factions.put(fChunk, symb);
					}
					sj2.add(symb);
				} else
					sj2.add("-");
			}
			sj2.add("\n");
		}
		sj.add(sj2.toString());
		if (!factions.isEmpty())
			sj.add(factions.entrySet().stream().map(entry -> "&2" + entry.getValue() + "&a = &2" + entry.getKey().getName()).collect(Collectors.joining("&7, ")));
		player.sendMessage(ColorUtils.color(sj.toString()));
	}

	@Cmd(player = true)
	public void unclaim(CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'infos.");
			return;
		}
		if (!OlympaFactionRole.OFFICER.hasPermission(faction.getRole(player))) {
			Set<FactionPlayer> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
			StringBuilder sb = new StringBuilder();
			if (!can.isEmpty())
				sb.append(" Demande à &4" + can.stream().map(FactionPlayer::getName).collect(Collectors.joining("&c, &4")) + "&c.");
			sendMessage(Prefix.FACTION, "&cTu n'a pas la permission." + sb.toString());
			return;
		}
		Chunk chunk = player.getLocation().getChunk();
		if (!faction.hasClaim(chunk)) {
			sendMessage(Prefix.FACTION, "&cCe claim n'appartient pas à ta faction.");
			return;
		}
		faction.unclaim(chunk);
		sendMessage(faction.getPlayers(), Prefix.FACTION, "&2%s&a a &lun&aclaim un chunk.", player.getName());
	}

	@Cmd(player = true)
	public void chat(CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'infos.");
			return;
		}
		FactionPlayer player = getOlympaPlayer();
		FactionChat askChat;
		FactionChat chat = player.getChat();
		if (cmd.getArgumentsLength() > 1) {
			askChat = FactionChat.get(cmd.getArgument(1));
			if (askChat == null) {
				sendMessage(Prefix.FACTION, "&cLe chat &4" + cmd.getArgument(1) + "&c n'existe pas.");
				return;
			} else if (chat != null && chat.equals(askChat)) {
				sendMessage(Prefix.FACTION, "&cTu utilise déjà le chat &4" + chat.getName() + "&c.");
				return;
			}
			player.setChat(askChat);
		} else if (chat == null)
			askChat = FactionChat.FACTION;
		else
			askChat = chat.getOther();
		sendMessage(Prefix.FACTION, "Tu parle désormais en chat &2" + askChat.getName() + "&a.");
	}

	@Cmd(player = true)
	public void show(CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (cmd.getArgumentsLength() == 1) {
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
		sj.add("&e&m----- " + color + faction.getName() + "&e&m -----");
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
		player.sendMessage(sj.toString());
	}
}
