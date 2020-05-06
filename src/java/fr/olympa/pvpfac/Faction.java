package fr.olympa.pvpfac;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.objects.OlympaPlayerInformations;
import fr.olympa.api.scoreboard.sign.DynamicLine;
import fr.olympa.api.scoreboard.sign.FixedLine;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.pvpfac.factions.objects.FactionPlayer;
import fr.olympa.pvpfac.factions.objects.OlympaFactionRole;

public class Faction extends Clan<Faction> {

	private static FixedLine header = new FixedLine("§e§oMa Faction:");
	private static DynamicLine<FactionPlayer> players = new DynamicLine<>((x) -> {
		Faction faction = x.getClan();
		Player p = x.getPlayer();
		StringJoiner joiner = new StringJoiner("\n");
		for (Entry<OlympaPlayerInformations, ClanPlayerInterface<Faction>> member : faction.getMembers()) {
			String memberName = member.getKey().getName();
			if (member.getValue() == null) {
				joiner.add("§c○ " + memberName);
			} else if (member.getValue() == x) {
				joiner.add("§6● §l" + memberName);
			} else {
				Location loc = member.getValue().getPlayer().getLocation();
				joiner.add("§e● " + memberName + " §l" + SpigotUtils.getDirectionToLocation(p, loc));
			}
		}
		return joiner.toString();
	}, 1, 0);
	List<String> oldName = new ArrayList<>();
	List<Chunk> claims = new ArrayList<>();
	String tag;

	String description;
	Location home;

	public Faction(ClansManager<Faction> manager, int id, String name, long chief, int maxSize) {
		super(manager, id, name, chief, maxSize);
	}

	public Faction(ClansManager<Faction> manager, int id, String name, long chief, int maxSize, long created) {
		super(manager, id, name, chief, maxSize, created);
	}

	public void claim(Chunk chunk) {
		// TODO Auto-generated method stub
		claims.add(chunk);
	}

	public boolean hasClaim(Chunk fChunk) {
		return claims.contains(fChunk);
	}

	public Set<Player> getOnlinePlayers(OlympaFactionRole officer) {
		return null;
	}

	public OlympaFactionRole getRole(Player player) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isOverClaimable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void memberJoin(ClanPlayerInterface<Faction> member) {
		super.memberJoin(member);

		Scoreboard scoreboard = PvPFaction.getInstance().scoreboards.getPlayerScoreboard(member);
		scoreboard.addLine(FixedLine.EMPTY_LINE);
		scoreboard.addLine(header);
		scoreboard.addLine(players);
	}

	@Override
	protected void removedOnlinePlayer(ClanPlayerInterface<Faction> oplayer) {
		super.removedOnlinePlayer(oplayer);

		PvPFaction.getInstance().scoreboards.removePlayerScoreboard(oplayer);
		PvPFaction.getInstance().scoreboards.create(oplayer);
	}

	public void unclaim(Chunk chunk) {
		// TODO Auto-generated method stub

		claims.remove(chunk);
	}
}
