package fr.olympa.pvpfac;

import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClansCommand;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.factions.FactionMsg;
import fr.olympa.pvpfac.factions.objects.OlympaFactionRole;

public class FactionCommand<T extends Clan<Faction>> extends ClansCommand<Faction> {

	public FactionCommand(FactionManager manager, String name, String description, OlympaPermission permission, String... aliases) {
		super(manager, name, description, permission, aliases);
	}

	@Cmd(player = true)
	public void claim() {
		Faction faction = getPlayerClan(false);
		if (FactionMsg.youHaveNoFaction(player, faction)) {
			sendMessage(Prefix.FACTION + "&cTu n'a pas de faction. &4/f help&c pour plus d'infos.");
			return;
		}
		if (!OlympaFactionRole.OFFICER.hasPermission(faction.getRole(player))) {
			Set<Player> can = faction.getOnlinePlayers(OlympaFactionRole.OFFICER);
			StringBuilder sb = new StringBuilder();
			if (!can.isEmpty()) {
				sb.append(" Demande à &4" + can.stream().map(Player::getName).collect(Collectors.joining("&c, &4")) + "&c.");
			}
			sendMessage(Prefix.FACTION + "&cTu n'a pas la permission." + sb.toString());
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
}
