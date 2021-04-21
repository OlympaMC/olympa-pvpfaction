package fr.olympa.pvpfac.faction;

import java.sql.SQLException;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.clans.ClansCommand;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.permission.OlympaSpigotPermission;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.PvPFactionPermission;
import fr.olympa.pvpfac.faction.chat.FactionChat;
import fr.olympa.pvpfac.faction.claim.FactionClaimPermLevel;
import fr.olympa.pvpfac.faction.claim.FactionClaim;
import fr.olympa.pvpfac.faction.claim.FactionClaimType;
import fr.olympa.pvpfac.faction.claim.FactionClaimsManager;
import fr.olympa.pvpfac.faction.map.FactionMap;
import fr.olympa.pvpfac.faction.utils.FactionMsg;
import fr.olympa.pvpfac.player.FactionPlayer;
import fr.olympa.pvpfac.player.FactionPlayerData;
import fr.olympa.pvpfac.player.FactionPlayerData.FactionRole;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class FactionCommand extends ClansCommand<Faction, FactionPlayerData> {

	public FactionCommand(FactionManager manager, String description, OlympaSpigotPermission permission, String... aliases) {
		super(manager, description, permission, aliases);
		this.addArgumentParser("FACTIONS", (player, arg) -> manager.getClans().stream().map(e -> e.getValue().getName()).collect(Collectors.toSet()),
				arg -> manager.getByName(arg),
				x -> String.format("&cLa faction &4%s&c n'existe pas.", x));
		this.addArgumentParser("FACTION_ROLE", FactionRole.class);
		this.addArgumentParser("FACTION_CLAIM_TYPE", FactionClaimType.class);
		this.addArgumentParser("CLAIM_PERM", FactionClaimPermLevel.class);
	}

	@Cmd(player = true, aliases = { "setdesc", "adddesc", "setdescription", "adddescription" }, args = { "100_lettres_max" }, min = 1)
	public void description(CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'infos.");
			return;
		}
		faction.updateDescription(Utils.capitalize(cmd.getFrom(1).toString().replace("\n", "")));
		Prefix.FACTION.sendMessage(faction.getPlayers(), "&2%s&a a changer la decription en &2%s&a.", player.getName(), faction.getDescription());
	}

	@Override
	@Cmd(player = true, aliases = { "settag", "addtag" }, args = { "6 lettres max" }, min = 1)
	public void tag(CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'infos.");
			return;
		}
		faction.updateTag(cmd.getArgument(0).toString().toUpperCase());
		Prefix.FACTION.sendMessage(faction.getPlayers(), "&2%s&a a changer le tag en &2%s&a.", player.getName(), faction.getTag());
	}

	@Cmd(player = true, aliases = { "p", "powers" }, args = { "PLAYERS", "INTEGER" })
	public void power(CommandContext cmd) {
		FactionPlayer fp;
		if (cmd.getArgumentsLength() != 0)
			try {
				fp = AccountProvider.get(cmd.getArgument(0, new String()));
				if (cmd.getArgumentsLength() != 1 && PvPFactionPermission.FACTION_BYPASS.hasSenderPermission(player) && cmd.getArgument(1) instanceof Integer) {
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

	@Cmd(player = true, args = "CLANPLAYER", min = 1)
	public void promote(CommandContext cmd) {
		FactionPlayerData player = cmd.getArgument(0);
		FactionRole above = player.getRole().getAbove();
		if (above == null)
			sendError("Le joueur %s est déjà au rang maximal possible !", player.getPlayerInformations().getName());
		else {
			player.setRole(above);
			sendSuccess("Tu as promu le joueur %s au rang %s!", player.getPlayerInformations().getName(), above.name);
		}
	}

	@Cmd(player = true, min = 1, args = { "FACTIONS|FACTION_CLAIM_TYPE" })
	public void forceclaim(CommandContext cmd) {
		if (!PvPFactionPermission.FACTION_BYPASS.hasSenderPermission(player)) {
			sendDoNotHavePermission();
			return;
		}

		Chunk chunk = player.getLocation().getChunk();
		FactionClaim claim = PvPFaction.getInstance().getClaimsManager().ofChunk(chunk);
		
		if (cmd.getArgument(0) instanceof Faction) {
			Faction faction = cmd.getArgument(0);
			claim.setFaction(faction);
			sendMessage(Prefix.FACTION, "Tu as claim pour &2%s&a.", faction.getName());
		} else if (cmd.getArgument(0) instanceof FactionClaimType) {
			FactionClaimType type = cmd.getArgument(0);
			if (type == null) {
				sendMessage(Prefix.FACTION, "&cFaction &4%s%c inconnu.", cmd.getArgument(0, new String()));
				return;
			}
			//sendMessage(Prefix.FACTION, "TODO > Claim &2%s&a.", type.name());
			claim.setType(type);
			sendMessage(Prefix.FACTION, "Tu as défini le claim comme &2%s&a.", type.name());
			//claimManager.updateClaim(claim);
		}
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
		//		if (!FactionRole.OFFICER.hasPermission(faction.getRole(player))) {
		//			Set<FactionPlayer> can = faction.getOnlinePlayers(FactionRole.OFFICER);
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
		FactionClaimsManager claimManager = PvPFaction.getInstance().getClaimsManager();
		FactionClaim claim = claimManager.ofChunk(chunk);
		Faction claimOldFaction = (claimOldFaction = claim.getFaction()) != null ? claimOldFaction.clone() : null;

		if (!claim.isOverClaimable()) {
			TextComponent text = new TextComponent(TextComponent.fromLegacyText("§cImpossible de &lsur&cclaim le chunk de la faction §4%s&c."
					.replace("%s", claim.getFaction() == null ? claim.getType().getName() : claim.getFaction().getNameColored(faction))));
			text.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(
					"§6TIPS §eCondition pour surclaim une faction:\n- La faction doit avoir moins de power que de claims\n- Le claim ne doit pas être entouré de claims §nuniquement§e de la même faction."))));
			sendComponents(text);
			return;
		}
		claim.setFaction(faction);
		claimManager.updateClaim(claim);
		if (claimOldFaction != null) {
			sendMessage(claimOldFaction.getPlayers(), Prefix.FACTION, "&4Vous avez perdu un claim, une autre faction vous a &lsur&4claim.");
			sendMessage(faction.getPlayers(), Prefix.FACTION, "&2%s&a a &lsur&aclaim un chunk de &c%s&a.", player.getName(), claimOldFaction.getName());
		} else
			sendMessage(faction.getPlayers(), Prefix.FACTION, "&2%s&a a claim un chunk.", player.getName());
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
		FactionClaimsManager claimManager = PvPFaction.getInstance().getClaimsManager();
		FactionClaim claim = claimManager.ofChunk(chunk);
		if (!faction.hasClaim(chunk)) {
			sendMessage(Prefix.FACTION, "&cCe claim n'appartient pas à ta faction.");
			return;
		}
		claim.setFaction(null);
		claimManager.updateClaim(claim);
		sendMessage(faction.getPlayers(), Prefix.FACTION, "&2%s&a a &lun&aclaim un chunk.", player.getName());
	}

	@Cmd(player = true, aliases = "m")
	public void map(CommandContext cmd) {
		FactionMap.sendMap(player, getPlayerClan(false));
	}

	@Cmd(player = true, aliases = "am")
	public void automap(CommandContext cmd) {
		FactionMap.toggleAutoMap(player);
	}

	@Cmd(player = true, aliases = "c", args = { "general|faction|allies" })
	public void chat(CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			//sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'infos.");
			return;
		}

		FactionPlayer player = getOlympaPlayer();
		FactionChat askChat;
		FactionChat chat = player.getChat();
		if (cmd.getArgumentsLength() > 0) {
			askChat = FactionChat.get(cmd.getArgument(0));
			if (askChat == null) {
				sendMessage(Prefix.FACTION, "&cLe chat &4" + cmd.getArgument(0) + "&c n'existe pas.");
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

	@Cmd(player = true, aliases = { "who", "f" }, args = "PLAYERS|FACTIONS")
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
				sendError();
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
		try {
			// BUG java.lang.NullPointerException -> pas plus d'infos, c'est la ligne du dessous
			sj.add("&3Joueurs déconnectés: &c" + faction.getOfflineFactionPlayers().stream().map(p -> p.getName()).collect(Collectors.joining("&b, &c")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		player.sendMessage(ColorUtils.color(sj.toString()));
	}
	

	@Cmd(player = true, aliases = "cp", args = {"set|info|tuto", "PLAYERS|FACTIONS", "CLAIM_PERM", "FACTION_ROLE"}, min = 1)
	public void claimperm(CommandContext cmd) {
		FactionClaim claim = PvPFaction.getInstance().getClaimsManager().ofChunk(getPlayer().getLocation().getChunk());
		
		if (cmd.getArgument(0).equals("info")) {
			sendMessage(Prefix.FACTION, "Todo !");
			return;
		
		}else if (cmd.getArgument(0).equals("tuto")) {
			String s = "§6Les permissions sont les suivantes :";
			for (FactionClaimPermLevel lvl : FactionClaimPermLevel.values())
				s += "\n§a" + lvl + "§e→ Permissions : " + lvl.getDesc();
			
			sendMessage(Prefix.FACTION, s);
			return;
		}else if (cmd.getArgumentsLength() < 3) {
			sendHelp(getSender());
			return;
		}
		FactionClaimPermLevel perm = cmd.getArgument(2);
		
		if (cmd.getArgument(1) instanceof Player) {
			FactionPlayer fp = AccountProvider.get(((Player)cmd.getArgument(1)).getUniqueId());
			if (claim.setPlayerLevel(fp, perm))
				sendMessage(Prefix.FACTION, "Permission de %s définie sur %s.", fp.getName(), perm.toString().toLowerCase());
			else
				sendMessage(Prefix.FACTION, "§ePermission de %s déjà définie sur %s.", fp.getName(), perm.toString().toLowerCase());
			
		}else if (cmd.getArgument(1) instanceof Faction && cmd.getArgumentsLength() >= 4) {
			Faction fac = cmd.getArgument(1);
			FactionRole role = cmd.getArgument(3);
			if (claim.setFactionLevel(fac, role, perm))
				sendMessage(Prefix.FACTION, "Permission du rôle %s de la faction %s définie sur %s.", role.name, fac.getName(), perm.toString().toLowerCase());
			else
				sendMessage(Prefix.FACTION, "§ePermission du rôle %s de la faction %s déjà définie sur %s.", role.name, fac.getName(), perm.toString().toLowerCase());
				
		}else
			sendIncorrectSyntax();
	}
}








