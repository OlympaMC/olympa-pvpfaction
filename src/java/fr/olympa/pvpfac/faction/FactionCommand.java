package fr.olympa.pvpfac.faction;

import java.sql.SQLException;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.provider.OlympaPlayerObject;
import fr.olympa.api.spigot.clans.ClansCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.PvPFactionPermission;
import fr.olympa.pvpfac.faction.chat.FactionChat;
import fr.olympa.pvpfac.faction.claim.FactionClaim;
import fr.olympa.pvpfac.faction.claim.FactionClaimPermLevel;
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

	public FactionCommand(final FactionManager manager, final OlympaSpigotPermission permission, final OlympaSpigotPermission managePermission, final String... aliases) {
		super(manager, "Permet de gérer les factions.", permission, managePermission, aliases);
		this.addArgumentParser("FACTION_ROLE", FactionRole.class, fr -> fr.name);
		this.addArgumentParser("FACTION_CLAIM_TYPE", FactionClaimType.class);
		this.addArgumentParser("CLAIM_PERM", FactionClaimPermLevel.class);
	}

	@Cmd(player = true, aliases = { "setdesc", "adddesc", "setdescription", "adddescription" }, args = { "100_lettres_max" }, min = 1)
	public void description(final CommandContext cmd) {
		final Faction faction = getPlayerClan(false);
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
			return;
		}
		faction.updateDescription(Utils.capitalize(cmd.getFrom(1).replace("\n", "")));
		Prefix.FACTION.sendMessage(faction.getPlayers(), "&2%s&a a changer la description en &2%s&a.", player.getName(), faction.getDescription());
	}

	@Override
	@Cmd(player = true, aliases = { "settag", "addtag" }, args = { "6 lettres max" }, min = 1)
	public void tag(final CommandContext cmd) {
		final Faction faction = getPlayerClan(false);
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
			return;
		}
		faction.updateTag(cmd.getArgument(0).toString().toUpperCase());
		Prefix.FACTION.sendMessage(faction.getPlayers(), "&2%s&a a changer le tag en &2%s&a.", player.getName(), faction.getTag());
	}

	@Cmd(player = true, aliases = { "p", "powers" }, args = { "PLAYERS", "INTEGER" })
	public void power(final CommandContext cmd) {
		final FactionPlayer fp;
		if (cmd.getArgumentsLength() == 0)
			fp = getOlympaPlayer();
		else
			try {
				fp = AccountProviderAPI.getter().get(cmd.getArgument(0, ""));
				if (cmd.getArgumentsLength() != 1 && PvPFactionPermission.FACTION_BYPASS.hasSenderPermission(player) && cmd.getArgument(1) instanceof Integer) {
					fp.setPower(cmd.getArgument(1));
					sendMessage(Prefix.FACTION, "&aLe power de &2" + fp.getName() + "&a est désormais de &2" + fp.getPower() + "&a/" + FactionPlayer.POWER_MAX + ".");
					return;
				}
			} catch (final SQLException e) {
				sendError();
				e.printStackTrace();
				return;
			}
		sendMessage(Prefix.FACTION, "&2" + fp.getName() + "&a a &2" + fp.getPower() + "&a/" + FactionPlayer.POWER_MAX + " de power.");
	}

	@Cmd(player = true, args = "CLANPLAYER", min = 1)
	public void promote(final CommandContext cmd) {
		final FactionPlayerData player = cmd.getArgument(0);
		final FactionRole above = player.getRole().getAbove();
		if (above == null)
			sendError("Le joueur %s est déjà au rang maximal possible !", player.getPlayerInformations().getName());
		else {
			player.setRole(above);
			sendSuccess("Tu as promu le joueur %s au rang %s!", player.getPlayerInformations().getName(), above.name);
		}
	}

	@Cmd(player = true, min = 1, args = { "CLAN|FACTION_CLAIM_TYPE" })
	public void forceclaim(final CommandContext cmd) {
		if (!PvPFactionPermission.FACTION_BYPASS.hasSenderPermission(player)) {
			sendDoNotHavePermission();
			return;
		}

		final Chunk chunk = player.getLocation().getChunk();
		final FactionClaim claim = PvPFaction.getInstance().getClaimsManager().ofChunk(chunk);

		if (cmd.getArgument(0) instanceof Faction) {
			final Faction faction = cmd.getArgument(0);
			claim.setFaction(faction);
			sendMessage(Prefix.FACTION, "Tu as claim pour &2%s&a.", faction.getName());
		} else if (cmd.getArgument(0) instanceof FactionClaimType) {
			final FactionClaimType type = cmd.getArgument(0);
			if (type == null) {
				sendMessage(Prefix.FACTION, "&cFaction &4%s%c inconnue.", cmd.getArgument(0, ""));
				return;
			}
			//sendMessage(Prefix.FACTION, "TODO > Claim &2%s&a.", type.name());
			claim.setType(type);
			sendMessage(Prefix.FACTION, "Tu as défini le claim comme &2%s&a.", type.name());
			//claimManager.updateClaim(claim);
		}
	}

	@Cmd(player = true, aliases = "cl")
	public void claim(final CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (cmd.getArgumentsLength() > 0 && PvPFactionPermission.FACTION_BYPASS.hasSenderPermission(player))
			try {
				faction = PvPFaction.getInstance().getFactionManager().get(cmd.getArgument(0));
			} catch (final SQLException e) {
				e.printStackTrace();
				sendError();
				return;
			}
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
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
		final Chunk chunk = player.getLocation().getChunk();
		if (faction.hasClaim(chunk)) {
			sendMessage(Prefix.FACTION, "&cCe claim appartient déjà à ta faction.");
			return;
		}
		final FactionClaimsManager claimManager = PvPFaction.getInstance().getClaimsManager();
		final FactionClaim claim = claimManager.ofChunk(chunk);
		Faction claimOldFaction = (claimOldFaction = claim.getFaction()) != null ? claimOldFaction.clone() : null;

		if (!claim.isOverClaimable()) {
			final TextComponent text = new TextComponent(
					TextComponent.fromLegacyText("§cImpossible de &lsur&cclaim le chunk de la faction §4%s&c.".replace(
							"%s",
							claim.getFaction() == null
									? claim.getType().getName()
									: claim.getFaction().getNameColored(faction))));
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
	public void unclaim(final CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (cmd.getArgumentsLength() > 0 && PvPFactionPermission.FACTION_BYPASS.hasSenderPermission(player))
			try {
				faction = PvPFaction.getInstance().getFactionManager().get(cmd.getArgument(0));
			} catch (final SQLException e) {
				e.printStackTrace();
				sendError();
				return;
			}
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
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
		final Chunk chunk = player.getLocation().getChunk();
		final FactionClaimsManager claimManager = PvPFaction.getInstance().getClaimsManager();
		final FactionClaim claim = claimManager.ofChunk(chunk);
		if (!faction.hasClaim(chunk)) {
			sendMessage(Prefix.FACTION, "&cCe claim n'appartient pas à ta faction.");
			return;
		}
		claim.setFaction(null);
		claimManager.updateClaim(claim);
		sendMessage(faction.getPlayers(), Prefix.FACTION, "&2%s&a a &lun&aclaim un chunk.", player.getName());
	}

	@Cmd(player = true, aliases = "m")
	public void map(final CommandContext cmd) {
		FactionMap.sendMap(player, getPlayerClan(false));
	}

	@Cmd(player = true, aliases = "am")
	public void automap(final CommandContext cmd) {
		FactionMap.toggleAutoMap(player);
	}

	@Cmd(player = true, aliases = "c", args = { "general|faction|allies" })
	public void chat(final CommandContext cmd) {
		final Faction faction = getPlayerClan(false);
		if (FactionMsg.youHaveNoFaction(player, faction))
			//sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
			return;

		final FactionPlayer player = getOlympaPlayer();
		final FactionChat askChat;
		final FactionChat chat = player.getChat();
		if (cmd.getArgumentsLength() > 0) {
			askChat = FactionChat.get(cmd.getArgument(0));
			if (askChat == null) {
				sendMessage(Prefix.FACTION, "&cLe chat &4" + cmd.getArgument(0) + "&c n'existe pas.");
				return;
			} else if (chat != null && chat == askChat) {
				sendMessage(Prefix.FACTION, "&cTu utilise déjà le chat &4" + chat.getName() + "&c.");
				return;
			}
			player.setChat(askChat);
		} else
			askChat = chat.getOther();
		player.setChat(askChat);
		sendMessage(Prefix.FACTION, "Tu parles désormais en chat &2" + askChat.getName() + "&a.");
	}

	@Cmd(player = true, aliases = { "who", "f" }, args = "PLAYERS|CLAN")
	public void show(final CommandContext cmd) {
		Faction faction = getPlayerClan(false);
		if (cmd.getArgumentsLength() == 0) {
			if (FactionMsg.youHaveNoFaction(player, faction)) {
				sendMessage(Prefix.FACTION, "&cTu n'a pas de faction. &4/f show <nom|tag|joueur>&c pour plus d'informations.");
				return;
			}
		} else {
			final FactionManager manager = PvPFaction.getInstance().getFactionManager();
			try {
				faction = manager.get(cmd.getArgument(1));
			} catch (final SQLException e) {
				e.printStackTrace();
				sendError();
				return;
			}
			if (faction == null) {
				sendMessage(Prefix.FACTION, "&cNom, tag ou joueur &4" + cmd.getArgument(1) + "&c inconnu.");
				return;
			}
		}
		final Faction playerFaction = ((FactionPlayer) getOlympaPlayer()).getClan();
		final ChatColor color = playerFaction != null && faction.getID() == playerFaction.getID() ? ChatColor.GREEN : ChatColor.RED;
		final StringJoiner sj = new StringJoiner("\n");
		sj.add("&e&m--------------&6 " + color + faction.getName() + " &e&m--------------&6");
		final String tag = faction.getTag();
		if (tag != null && !tag.isBlank())
			sj.add("&3Tag: &b" + tag);
		final String description = faction.getDescription();
		if (description != null && !description.isBlank())
			sj.add("&3Description: &b" + description);
		final long creationTime = faction.getCreationTime();
		sj.add("&3Crée le : &b" + Utils.timestampToDate(creationTime) + "&3 (" + Utils.timestampToDuration(creationTime) + ")");
		sj.add("&3Claims/Power/MaxPower : &b" + faction.getClaimsPowerMaxPower());
		sj.add("&3Joueurs max: &b" + faction.getMaxSize());
		sj.add("&3Joueurs connectés: &a" + faction.getOnlineFactionPlayers().stream().map(OlympaPlayerObject::getName).collect(Collectors.joining("&b, &a")));
		try {
			// BUG java.lang.NullPointerException -> pas plus d'informations, c'est la ligne du dessous
			sj.add("&3Joueurs déconnectés: &c" + faction.getOfflineFactionPlayers().stream().map(OlympaPlayerObject::getName).collect(Collectors.joining("&b, &c")));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		player.sendMessage(ColorUtils.color(sj.toString()));
	}

	@Cmd(player = true, aliases = "cp", args = { "set|info|tuto", "PLAYERS|CLAN", "CLAIM_PERM", "FACTION_ROLE" }, min = 1)
	public void claimperm(final CommandContext cmd) {
		final FactionClaim claim = PvPFaction.getInstance().getClaimsManager().ofChunk(getPlayer().getLocation().getChunk());

		if ("info".equals(cmd.getArgument(0))) {
			sendMessage(Prefix.FACTION, "Todo !");
			return;

		} else if ("tuto".equals(cmd.getArgument(0))) {
			final StringBuilder s = new StringBuilder("§6Les permissions sont les suivantes :");
			for (final FactionClaimPermLevel lvl : FactionClaimPermLevel.values())
				s.append("\n§a").append(lvl).append("§e→ Permissions : ").append(lvl.getDesc());

			sendMessage(Prefix.FACTION, s.toString());
			return;
		} else if (cmd.getArgumentsLength() < 3) {
			sendHelp(getSender());
			return;
		}
		final FactionClaimPermLevel perm = cmd.getArgument(2);

		if (cmd.getArgument(1) instanceof Player) {
			final FactionPlayer fp = AccountProviderAPI.getter().get(((Player) cmd.getArgument(1)).getUniqueId());
			if (claim.setPlayerLevel(fp, perm))
				sendMessage(Prefix.FACTION, "Permission de %s définie sur %s.", fp.getName(), perm.toString().toLowerCase());
			else
				sendMessage(Prefix.FACTION, "§ePermission de %s déjà définie sur %s.", fp.getName(), perm.toString().toLowerCase());

		} else if (cmd.getArgument(1) instanceof Faction && cmd.getArgumentsLength() >= 4) {
			final Faction fac = cmd.getArgument(1);
			final FactionRole role = cmd.getArgument(3);
			if (claim.setFactionLevel(fac, role, perm))
				sendMessage(Prefix.FACTION, "Permission du rôle %s de la faction %s définie sur %s.", role.name, fac.getName(), perm.toString().toLowerCase());
			else
				sendMessage(Prefix.FACTION, "§ePermission du rôle %s de la faction %s déjà définie sur %s.", role.name, fac.getName(), perm.toString().toLowerCase());

		} else
			sendIncorrectSyntax();
	}
}
