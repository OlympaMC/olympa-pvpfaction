package fr.olympa.pvpfac.faction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.google.gson.Gson;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.scoreboard.sign.lines.FixedLine;
import fr.olympa.api.scoreboard.sign.lines.TimerLine;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.player.FactionPlayer;

public class Faction extends Clan<Faction> {
	
	private static FixedLine<FactionPlayer> header = new FixedLine<>("§e§oMa Faction:");
	private static TimerLine<FactionPlayer> players = new TimerLine<>((x) -> {

		Faction faction = x.getClan();
		Player p = x.getPlayer();
		StringJoiner joiner = new StringJoiner("\n");
		for (Entry<OlympaPlayerInformations, ClanPlayerInterface<Faction>> member : faction.getMembers()) {
			String memberName = member.getKey().getName();
			if (member.getValue() == null)
				joiner.add("§c○ " + memberName);
			else if (member.getValue() == x)
				joiner.add("§6● §l" + memberName);
			else {
				Location loc = member.getValue().getPlayer().getLocation();
				joiner.add("§e● " + memberName + " §l" + SpigotUtils.getDirectionToLocation(p, loc));
			}
		}
		return joiner.toString();
	}, PvPFaction.getInstance(), 10);
	//	List<String> oldName = new ArrayList<>();

	Set<FactionClaim> claims = new HashSet<>();
	String tag;
	String description;
	Location home;
	
	public Faction(ClansManager<Faction> manager, int id, String name, long chief, int maxSize) {
		super(manager, id, name, chief, maxSize);
	}
	
	public Faction(ClansManager<Faction> manager, int id, String name, long chief, int maxSize, double money, long created, String tag, String description, Location home, Set<FactionClaim> claims) {
		super(manager, id, name, chief, maxSize, money, created);
		this.tag = tag;
		this.description = description;
		this.home = home;
		this.claims = claims;
	}
	
	public void claim(Chunk chunk) {
		claims.add(new FactionClaim(chunk));
	}

	public Set<FactionPlayer> getOnlinePlayers() {
		return getPlayers().stream().map(p -> (FactionPlayer) AccountProvider.get(p.getUniqueId())).collect(Collectors.toSet());
	}
	
	public OlympaFactionRole getRole(Player player) {
		return null;
	}

	public int getPower() {
		return getPlayers().stream().mapToInt(p -> ((FactionPlayer) AccountProvider.get(p.getUniqueId())).getPower()).sum();
	}
	
	public boolean hasClaim(Chunk chunk) {
		return claims.stream().anyMatch(claim -> claim.isChunk(chunk));
	}
	
	public boolean isOverClaimable() {
		return false;
	}
	
	@Override
	public void memberJoin(ClanPlayerInterface<Faction> member) {
		super.memberJoin(member);
		
		Scoreboard<FactionPlayer> scoreboard = PvPFaction.getInstance().scoreboards.getPlayerScoreboard((FactionPlayer) member);
		scoreboard.addLine(FixedLine.EMPTY_LINE);
		scoreboard.addLine(header);
		scoreboard.addLine(players);
	}
	
	@Override
	protected void removedOnlinePlayer(ClanPlayerInterface<Faction> oplayer) {
		super.removedOnlinePlayer(oplayer);

		PvPFaction.getInstance().scoreboards.removePlayerScoreboard((FactionPlayer) oplayer);
		PvPFaction.getInstance().scoreboards.create((FactionPlayer) oplayer);
	}
	
	public boolean unclaim(Chunk chunk) {
		return claims.removeIf(claim -> claim.isChunk(chunk));
	}
	
	private void updateClaims() {
		try {
			PreparedStatement statement = manager.updateClanMoneyStatement.getStatement();
			statement.setString(1, new Gson().toJson(claims));
			statement.setInt(2, id);
			statement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
			broadcast("Une erreur est survenue.");
		}
	}

	public Set<FactionPlayer> getOnlinePlayers(OlympaFactionRole officer) {
		return null;
	}
}
