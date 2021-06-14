package fr.olympa.pvpfac.factionold;

import fr.olympa.api.spigot.command.OlympaCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OldFactionCommand extends OlympaCommand {

	public OldFactionCommand(final Plugin plugin) {
		super(plugin, "faction", "f", "factions");
		addArgs(true, "create", "invite", "join", "who", "claim", "autoclaim", "map", "automap", "promote", "demote", "chat", "power");
		addArgs(false, "joueur");
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		return false;
		/*if (player != null) {
			FactionPlayer fp = AccountProviderAPI.getter().get(player.getUniqueId());
			Faction faction = fp.getFaction();
			if (args.length > 0) {
				switch (args[0]) {
				case "create":
					if (faction != null) {
						sendMessage(Prefix.FACTION + "&cTu as déjà une faction. Quitte-la pour pouvoir en crée une.");
						return false;
					}
					if (args.length < 2) {
						FactionHandler.addPlayerTryingToCreateFaction(player);
						sendMessage(Prefix.FACTION + "&aQuel sera le nom de ta faction ? &e(réponds dans le chat, écrit &cAnnuler&e pour annuler)&a.");
						// sendUsage(label);
						return false;
					}
					String tag = null;
					if (args.length > 2) {
						tag = args[2];
					}
					faction = new OlympaFaction(args[1], tag, player);
					faction.addConnected(player);
					FactionHandler.addFaction(faction);
					fp.setFaction(faction);
					sendMessage(Prefix.FACTION, "Ta faction &2" + faction.getName() + "&a a été crée.");
					List<String> s = new ArrayList<>();
					s.add("%player arrive en force avec sa faction %faction.");
					s.add("%player n'a pas trouvé un meilleur nom de faction que %faction ?");
					s.add("%player est fier de vous présenter %faction.");
					s.add("%player annonce la dictature avec %faction !");
					s.add("%player vous présente sa faction %faction.");
					s.add("%player est à l'aise avec %faction.");
					s.add("Bonne chance pour %player avec sa faction %faction.");
					int randomIndex = new Random().nextInt(s.size());

					Bukkit.broadcastMessage(Prefix.FACTION + ColorUtils.color(s.get(randomIndex).replace("%player", "&2" + player.getName() + "&a").replace("%faction", "&2" + faction.getName() + "&a")));
					break;
				case "invite":
					if (FactionMsg.youCantWithConsole(sender)) {
						sendImpossibleWithConsole();
						return false;
					}
					if (args.length < 2) {
						sendUsage(label);
						return false;
					}
					if (FactionMsg.youHaveNoFaction(player, faction)) {
						sendMessage(Prefix.FACTION + "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
						return false;
					}
					if (!OlympaFactionRole.OFFICER.hasPermission(faction.getRole(player))) {
						Set<Player> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
						StringBuilder sb = new StringBuilder();
						if (can.isEmpty()) {
							sb.append(" Demande à &4" + can.stream().map(Player::getName).collect(Collectors.joining("&c, &4")) + "&c.");
						}
						sendMessage(Prefix.FACTION + "&cTu n'a pas la permission." + sb.toString());
						return false;
					}
					Player target = Bukkit.getPlayer(args[1]);
					if (target == null) {
						sendUnknownPlayer(args[1]);
						return false;
					}
					FactionPlayer fpTarget = AccountProviderAPI.getter().get(target.getUniqueId());
					OlympaFaction factionTarget = fpTarget.getFaction();

					if (factionTarget != null) {
						sendMessage(Prefix.FACTION, "&4" + target.getName() + "&c a déjà une faction.");
						return false;
					}

					FactionHandler.addInvite(target, player, faction);
					TextComponent textComponent = new TextComponent();
					TextComponent textComponent2 = new TextComponent(Prefix.FACTION.toString() + "&2" + player.getName() + " &aa inviter &2" + target.getName() + "&a.");

					textComponent2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Refuser l'invitation pour " + target.getName()).color(ChatColor.RED).create()));
					textComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/f cancel " + target.getName()));
					textComponent.addExtra(textComponent2);

					Set<Player> connected = faction.getOnlinePlayers().stream().filter(p -> !p.getUniqueId().equals(player.getUniqueId())).collect(Collectors.toSet());
					for (Player p : connected) {
						p.spigot().sendMessage(textComponent);
					}
					sendMessage(Prefix.FACTION, "Tu as invité &2" + target.getName() + " &a.");
					textComponent = FactionUtils.getFirstSep();

					textComponent2 = new TextComponent("Tu as été inviter à rejoindre la faction ");
					textComponent2.setColor(ChatColor.YELLOW);
					textComponent.addExtra(textComponent2);

					textComponent2 = new TextComponent(faction.getName());
					textComponent2.setColor(ChatColor.GOLD);
					textComponent2.setBold(true);
					textComponent.addExtra(textComponent2);

					textComponent2 = new TextComponent(" par ");
					textComponent2.setColor(ChatColor.YELLOW);
					textComponent.addExtra(textComponent2);

					textComponent2 = new TextComponent(player.getName());
					textComponent2.setColor(ChatColor.GOLD);
					textComponent2.setBold(true);
					textComponent.addExtra(textComponent2);

					textComponent2 = new TextComponent(". Tu as 1 minute pour accepter ou refuser l'invitation.\n\n");
					textComponent2.setColor(ChatColor.YELLOW);
					textComponent.addExtra(textComponent2);

					textComponent2 = new TextComponent("[REJOINDRE]\n\n");
					textComponent2.setColor(ChatColor.GREEN);
					textComponent2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Rejoinds la faction " + faction.getName()).color(ChatColor.GREEN).create()));
					textComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/f join " + faction.getName()));
					textComponent.addExtra(textComponent2);

					textComponent2 = new TextComponent("[REFUSER]\n");
					textComponent2.setColor(ChatColor.DARK_RED);
					textComponent2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Refuser l'invitation de " + faction.getName()).color(ChatColor.RED).create()));
					textComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/f refuse " + faction.getName()));
					textComponent.addExtra(textComponent2);

					textComponent.addExtra(FactionUtils.getLastSep());

					target.spigot().sendMessage(textComponent);
					break;
				case "join":
					if (args.length < 2) {
						sendUsage(label);
						return false;
					}
					if (!(sender instanceof Player)) {
						if (FactionMsg.needArg3ToSelectFaction(sender, label, args)) {
							return false;
						}
						OfflinePlayer targetOffline = Bukkit.getPlayer(args[1]);
						if (targetOffline == null) {
							targetOffline = Bukkit.getOfflinePlayer(args[1]);
							if (targetOffline == null) {
								sendUnknownPlayer(args[1]);
							}
						}
						// TODO get
						player.getLocation();
						faction = FactionHandler.getFaction(args[3]);
					} else {
						OlympaFactionInvite invite = FactionHandler.getInvite(player, args[1]);
						if (invite == null) {
							sendMessage(Prefix.FACTION + "" + Prefix.ERROR + "Il encore impossible de faire des demande pour rejoindre les factions. Tu dois être invité.");
							return false;
						}
						OlympaFaction factionInvite = invite.getFaction();
						if (faction != null) {
							if (faction.getId() == factionInvite.getId()) {
								sendMessage(Prefix.FACTION, Prefix.ERROR + "Tu es déjà dans la faction &4" + faction.getName() + "&c.");
							} else {
								sendMessage(Prefix.FACTION, Prefix.ERROR + "Tu as déjà une faction.");
							}
							return false;
						}

						invite.getFaction().join(player);
						fp.setFaction(factionInvite);
						player.sendMessage(Prefix.FACTION + "&aTu as rejoint la faction &2" + factionInvite.getName() + "&a.");
					}
					break;
				case "refuse":
					if (args.length < 2) {
						sendUsage(label);
						return false;
					}
					OlympaFactionInvite invite = FactionHandler.getInvite(player, args[1]);
					if (invite == null) {
						sendMessage(Prefix.FACTION + "" + Prefix.ERROR + "Tu as reçu aucune invitation de &4" + args[1] + "&c.");
						return false;
					}
					faction = invite.getFaction();

					invite.getFaction().refuse(player);
					FactionHandler.removeInvite(invite);
					sendMessage(Prefix.FACTION, "&aTu as refusée l'invitation de &2" + faction.getName() + "&a.");
					break;
				case "cancel":
					if (args.length < 2) {
						sendUsage(label);
						return false;
					}
					if (FactionMsg.youHaveNoFaction(player, faction)) {
						sendMessage(Prefix.FACTION + "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
						return false;
					}
					target = Bukkit.getPlayer(args[1]);
					if (target == null) {
						sendUnknownPlayer(args[1]);
					}
					invite = FactionHandler.getInviteByFaction(target, faction);
					if (invite == null) {
						sendMessage(Prefix.FACTION + "" + Prefix.ERROR + "Ta faction n'a pas inviter &4" + target.getName() + "&c.");
						return false;
					}
					faction = invite.getFaction();

					FactionHandler.removeInvite(invite);
					invite.getFaction().cancel(player);
					sendMessage(Prefix.FACTION, "&cTu as annuler l'invitation pour &4" + target.getName() + "&c.");
					sendMessage(target, Prefix.FACTION, "&cL'invitation pour rejoindre &4" + faction.getName() + "&c a expirée.");
					break;
				case "who":
				case "show":
				case "f":
					if (args.length == 1) {
						if (FactionMsg.youHaveNoFaction(player, faction)) {
							sendMessage(Prefix.FACTION + "&cTu n'a pas de faction. &4/f " + label + "&c <faction>&c pour afficher une faction.");
							return false;
						}
					} else if (args.length >= 2) {
						faction = FactionHandler.getFaction(args[2]);
						if (faction == null) {
							sendError("La faction, le tag ou joueur &4" + args[2] + "&c n'existe pas.");
							return false;
						}
					}
					textComponent = FactionUtils.getFirstSep();

					ChatColor color1 = ChatColor.AQUA;
					ChatColor color2 = ChatColor.DARK_AQUA;

					textComponent2 = new TextComponent("Nom : ");
					textComponent2.setColor(color2);
					textComponent.addExtra(textComponent2);
					textComponent2 = new TextComponent(faction.getName() + "\n");
					textComponent2.setColor(color1);
					textComponent.addExtra(textComponent2);

					if (faction.getDescription() != null) {
						textComponent2 = new TextComponent("Description :");
						textComponent2.setColor(color2);
						textComponent.addExtra(textComponent2);
						textComponent2 = new TextComponent(faction.getDescription() + "\n");
						textComponent2.setColor(color1);
						textComponent.addExtra(textComponent2);
					}

					textComponent2 = new TextComponent("Claim/Power/PowerMax : ");
					textComponent2.setColor(color2);
					textComponent.addExtra(textComponent2);
					textComponent2 = new TextComponent("En dév");
					textComponent2.setColor(ChatColor.RED);
					textComponent.addExtra(textComponent2);
					textComponent2 = new TextComponent("/" + faction.getPower() + "/" + faction.getMaxPower() + "\n");
					textComponent2.setColor(color1);
					textComponent.addExtra(textComponent2);

					textComponent2 = new TextComponent("Alliés : ");
					textComponent2.setColor(ChatColor.LIGHT_PURPLE);
					textComponent.addExtra(textComponent2);
					textComponent2 = new TextComponent("En dév" + "\n");
					textComponent2.setColor(ChatColor.RED);
					textComponent.addExtra(textComponent2);

					textComponent2 = new TextComponent("Trêve : ");
					textComponent2.setColor(ChatColor.WHITE);
					textComponent.addExtra(textComponent2);
					textComponent2 = new TextComponent("En dév" + "\n");
					textComponent2.setColor(ChatColor.RED);
					textComponent.addExtra(textComponent2);

					textComponent2 = new TextComponent("Ennemis : ");
					textComponent2.setColor(ChatColor.RED);
					textComponent.addExtra(textComponent2);
					textComponent2 = new TextComponent("En dév" + "\n");
					textComponent2.setColor(ChatColor.RED);
					textComponent.addExtra(textComponent2);

					Set<Player> onlinePlayer = faction.getOnlinePlayers();
					Set<OfflinePlayer> offlinePlayer = faction.getOfflinePlayer();
					int online = onlinePlayer.size();
					int offline = offlinePlayer.size();
					int max = online + offline;
					textComponent2 = new TextComponent("Connectés %on/%max :".replace("%on", String.valueOf(online)).replace("%max", String.valueOf(max)));
					textComponent2.setColor(color1);
					textComponent.addExtra(textComponent2);
					textComponent2 = new TextComponent(String.join(", ", onlinePlayer.stream().map(Player::getName).collect(Collectors.toSet())) + "\n");
					textComponent2.setColor(color2);
					textComponent.addExtra(textComponent2);

					textComponent2 = new TextComponent("Déconnectés  :");
					textComponent2.setColor(color1);
					textComponent.addExtra(textComponent2);
					textComponent2 = new TextComponent(String.join(", ", offlinePlayer.stream().map(OfflinePlayer::getName).collect(Collectors.toSet())) + "\n");
					textComponent2.setColor(color2);
					textComponent.addExtra(textComponent2);

					textComponent.addExtra(FactionUtils.getLastSep());
					sendMessage(textComponent);
					break;
				case "claim":
					if (FactionMsg.youHaveNoFaction(player, faction)) {
						sendMessage(Prefix.FACTION + "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
						return false;
					}
					if (!OlympaFactionRole.OFFICER.hasPermission(faction.getRole(player))) {
						Set<Player> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
						StringBuilder sb = new StringBuilder();
						if (!can.isEmpty()) {
							sb.append(" Demande à &4" + can.stream().map(Player::getName).collect(Collectors.joining("&c, &4")) + "&c.");
						}
						sendMessage(Prefix.FACTION + "&cTu n'a pas la permission." + sb.toString());
						return false;
					}
					Chunk chunk = player.getLocation().getChunk();
					OlympaFaction fChunk = FactionHandler.getFaction(player.getLocation().getChunk());
					if (faction.isSame(fChunk)) {
						sendMessage(Prefix.FACTION, "&cCe claim appartient déjà a ta faction.");
						return false;
					}

					if (fChunk != null) {
						if (!fChunk.isOverClaimable()) {
							sendMessage(Prefix.FACTION, "&cImpossible de surclaim &4" + fChunk.getName() + "&c.");
							return false;
						}
						fChunk.unclaim(chunk);
					}
					faction.claim(chunk);
					sendMessage(faction.getOnlinePlayers(), Prefix.FACTION, "&2" + player.getName() + "&a a claim un chunk.");

					break;
				case "autoclaim":
					if (FactionMsg.youHaveNoFaction(player, faction)) {
						sendMessage(Prefix.FACTION + "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
						return false;
					}
					break;
				case "map":
					break;
				case "automap":
					break;
				case "kick":
					if (FactionMsg.youHaveNoFaction(player, faction)) {
						sendMessage(Prefix.FACTION + "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
						return false;
					}
					if (args.length < 2) {
						sendUsage(label);
						return false;
					}
					if (!OlympaFactionRole.OFFICER.hasPermission(faction.getRole(player))) {
						Set<Player> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
						StringBuilder sb = new StringBuilder();
						if (!can.isEmpty()) {
							sb.append(" Demande à &4" + can.stream().map(Player::getName).collect(Collectors.joining("&c, &4")) + "&c.");
						}
						sendMessage(Prefix.FACTION + "&cTu n'a pas la permission." + sb.toString());
						return false;
					}
					break;
				case "promote":
					if (FactionMsg.youHaveNoFaction(player, faction)) {
						sendMessage(Prefix.FACTION + "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
						return false;
					}
					if (args.length < 2) {
						sendUsage(label);
						return false;
					}
					target = Bukkit.getPlayer(args[1]);
					if (target == null) {
						sendUnknownPlayer(args[1]);
						return false;
					}
					fpTarget = AccountProviderAPI.getter().get(target.getUniqueId());
					factionTarget = fpTarget.getFaction();
					if (factionTarget.getId() != faction.getId()) {
						sendError("Le joueur &4" + target.getName() + "&c n'est pas dans ta faction.");
						return false;
					}
					OlympaFactionRole role = faction.getRole(player);
					OlympaFactionRole targetRole = faction.getRole(target);
					if (!OlympaFactionRole.OFFICER.hasPermission(role)) {
						Set<Player> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
						StringBuilder sb = new StringBuilder();
						if (!can.isEmpty()) {
							sb.append(" Demande à &4" + can.stream().map(Player::getName).collect(Collectors.joining("&c, &4")) + "&c.");
						}
						sendMessage(Prefix.FACTION + "&cTu n'a pas la permission." + sb.toString());
						return false;
					} else if (OlympaFactionRole.OFFICER.hasPermission(targetRole)) {
						if (OlympaFactionRole.LEADER.hasPermission(role)) {
							sendMessage(Prefix.FACTION + "&cImpossible de mettre &4" + target.getName() + " " + OlympaFactionRole.LEADER.getName() + "&c. Tu dois faire &4/f leader " + target.getName() + "&c.");
						} else {
							sendMessage(Prefix.FACTION + "&cTu as vraiement essaye de pomote &4" + target.getName() + " " + OlympaFactionRole.LEADER.getName() + "&c?");
						}
						return false;
					}
					OlympaFactionRole newRole = factionTarget.promote(target);
					sendMessage(target, Prefix.FACTION, "Tu as été promote &2" + newRole.getName() + "&a.");
					List<Player> onlineFactionPlayers = factionTarget.getOnlinePlayers().stream().filter(p -> p.getUniqueId() != target.getUniqueId()).collect(Collectors.toList());
					sendMessage(onlineFactionPlayers, Prefix.FACTION, "&2" + target.getName() + " &aest promote &2" + newRole.getName() + "&a.");
					break;
				case "demote":
					if (FactionMsg.youHaveNoFaction(player, faction)) {
						return false;
					}
					if (args.length < 2) {
						sendUsage(label);
						return false;
					}
					target = Bukkit.getPlayer(args[1]);
					if (target == null) {
						sendError("Le joueur &4" + args[1] + "&c n'existe pas.");
						return false;
					}
					fpTarget = AccountProviderAPI.getter().get(target.getUniqueId());
					factionTarget = fpTarget.getFaction();
					if (factionTarget.getId() != faction.getId()) {
						sendError("Le joueur &4" + target.getName() + "&c n'est pas dans ta faction.");
						return false;
					}
					role = faction.getRole(player);
					targetRole = faction.getRole(target);
					if (!OlympaFactionRole.OFFICER.hasPermission(role)) {
						Set<Player> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
						StringBuilder sb = new StringBuilder();
						if (!can.isEmpty()) {
							sb.append(" Demande à &4" + can.stream().map(Player::getName).collect(Collectors.joining("&c, &4")) + "&c.");
						}
						sendMessage(Prefix.FACTION + "&cTu n'a pas la permission." + sb.toString());
						return false;
					} else if (OlympaFactionRole.OFFICER.hasPermission(targetRole) && !OlympaFactionRole.LEADER.hasPermission(role)) {
						sendMessage(Prefix.FACTION + "&cTu as vraiement essaye de demote &4" + target.getName() + "&c?");
						return false;
					} else if (OlympaFactionRole.RECRUT.hasPermission(targetRole)) {
						sendMessage(Prefix.FACTION + "&cImpossible de demote un &4" + targetRole.getName() + "&c.");
						return false;
					}
					newRole = factionTarget.demote(target);
					sendMessage(target, Prefix.FACTION, "&cTu as été démote &4" + newRole.getName() + "&c.");
					onlineFactionPlayers = factionTarget.getOnlinePlayers().stream().filter(p -> p.getUniqueId() != target.getUniqueId()).collect(Collectors.toList());
					sendMessage(onlineFactionPlayers, Prefix.FACTION, "&4" + target.getName() + " &cest demote &4" + newRole.getName() + "&c.");
					break;
				case "c":
				case "chat":
					if (FactionMsg.youHaveNoFaction(player, faction)) {
						sendMessage(Prefix.FACTION + "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
						return false;
					}
					FactionChat askChat;
					FactionChat chat = fp.getChat();
					if (args.length > 1) {
						askChat = FactionChat.get(args[1]);
						if (askChat == null) {
							sendMessage(Prefix.FACTION, "&cLe chat &4" + args[1] + "&c n'existe pas.");
							return false;
						} else if (chat != null && chat.equals(askChat)) {
							sendMessage(Prefix.FACTION, "&cTu utilise déjà le chat &4" + chat.getName() + "&c.");
							return false;
						}
						fp.setChat(askChat);
					} else {
						if (chat == null) {
							askChat = FactionChat.FACTION;
						} else {
							askChat = chat.getOther();
						}
					}
					sendMessage(Prefix.FACTION, "Tu parle désormais en chat &2" + askChat.getName() + "&a.");
					break;
				case "p":
				case "power":
					if (args.length >= 2) {
						target = Bukkit.getPlayer(args[1]);
						if (target == null) {
							sendUnknownPlayer(args[1]);
							return false;
						}
						fpTarget = AccountProviderAPI.getter().get(target.getUniqueId());
						factionTarget = fpTarget.getFaction();
						sendMessage(Prefix.FACTION, "&2" + target.getName() + "&a a &2" + fpTarget.getPower() + "&a power.");
					} else {
						target = player;
						fpTarget = fp;
						sendMessage(Prefix.FACTION, "&aTu a &2" + fpTarget.getPower() + "&a power.");
					}
					break;
				case "sethome":
					if (FactionMsg.youHaveNoFaction(player, faction)) {
						sendMessage(Prefix.FACTION + "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
						return false;
					}
					if (!OlympaFactionRole.OFFICER.hasPermission(faction.getRole(player))) {
						Set<Player> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
						StringBuilder sb = new StringBuilder();
						if (!can.isEmpty()) {
							sb.append(" Demande à &4" + can.stream().map(Player::getName).collect(Collectors.joining("&c, &4")) + "&c.");
						}
						sendMessage(Prefix.FACTION + "&cTu n'a pas la permission." + sb.toString());
						return false;
					}
					faction.setHome(player.getLocation());
					sendMessage(faction.getOnlinePlayers(), Prefix.FACTION, "Le f home a été défini par &2" + player.getName() + "&a.");
					break;
				case "home":
					if (FactionMsg.youHaveNoFaction(player, faction)) {
						sendMessage(Prefix.FACTION + "&cTu n'a pas de faction. &4/f help&c pour plus d'informations.");
						return false;
					}
					Location home = faction.getHome();
					if (home == null) {
						Set<Player> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
						StringBuilder sb = new StringBuilder();
						if (!can.isEmpty()) {
							sb.append(" Demande à &4" + can.stream().map(Player::getName).collect(Collectors.joining("&c, &4")) + "&c.");
						}
						sendMessage(Prefix.FACTION, "&cIl n'y a pas de home set." + sb.toString());
						return false;
					}
					player.teleport(home);
					sendMessage(Prefix.FACTION, "&eTéléportation au f home.");
					break;
				}
				return false;
			}
			// send help
		} else {
			// console
		}

		return false;*/
	}

	@Override
	public @Nullable List<String> onTabComplete(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
