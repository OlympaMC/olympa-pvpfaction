package fr.olympa.pvpfac.faction;

import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Chunk;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClansCommand;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.player.FactionPlayer;

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
			sendMessage(Prefix.FACTION, "&cCe claim appartient déjà a ta faction.");
			return;
		}
		Set<Entry<Integer, Faction>> clans = PvPFaction.getInstance().getFactionManager().getClans();
		Faction fChunk = clans.stream().filter(c -> c.getValue().hasClaim(chunk)).map(e -> e.getValue()).findFirst().orElse(null);
		
		if (fChunk != null) {
			if (!fChunk.isOverClaimable()) {
				sendMessage(Prefix.FACTION, "&cImpossible de surclaim &4" + fChunk.getName() + "&c.");
				return;
			}
			fChunk.unclaim(chunk);
		}
		faction.claim(chunk);
		sendMessage(faction.getPlayers(), Prefix.FACTION, "&2" + player.getName() + "&a a claim un chunk.");
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
}
