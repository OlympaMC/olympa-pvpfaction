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
import fr.olympa.api.clans.OlympaFactionRole;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.claim.FactionClaim;
import fr.olympa.pvpfac.player.FactionPlayer;

public class Faction extends Clan<Faction> {

	private static FixedLine<Scoreboard<FactionPlayer>> header = new FixedLine<>("§7§oMa Faction:");
	private static TimerLine<Scoreboard<FactionPlayer>> players = new TimerLine<>((x) -> {
		FactionPlayer fp = x.getOlympaPlayer();
		Faction faction = fp.getClan();
		Player p = fp.getPlayer();
		StringJoiner joiner = new StringJoiner("\n");
		for (Entry<OlympaPlayerInformations, ClanPlayerInterface<Faction>> member : faction.getMembers()) {
			String memberName = member.getKey().getName();
			if (member.getValue() == null)
				joiner.add("§c○ " + memberName);
			else if (member.getValue() == fp)
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

	public Set<FactionClaim> getClaims() {
		return claims;
	}

	public String getTag() {
		return tag;
	}

	public String getDescription() {
		return description;
	}

	public Location getHome() {
		return home;
	}
	
	public Set<FactionPlayer> getOnlineFactionPlayers() {
		return getPlayers().stream().map(p -> (FactionPlayer) AccountProvider.get(p.getUniqueId())).collect(Collectors.toSet());
	}
	
	public Set<FactionPlayer> getOfflineFactionPlayers() {
		return members.values().stream().filter(entry -> entry.getValue() == null).map(entry -> (FactionPlayer) AccountProvider.get(entry.getKey().getUUID())).collect(Collectors.toSet());
	}
	
	public int getPower() {
		return getPlayers().stream().mapToInt(p -> ((FactionPlayer) AccountProvider.get(p.getUniqueId())).getPower()).sum();
	}

	public boolean hasClaim(Chunk chunk) {
		return !claims.isEmpty() && claims.stream().anyMatch(claim -> claim.isChunk(chunk));
	}

	public boolean isOverClaimable() {
		return claims.size() > getPower();
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

	public void claim(Chunk chunk) {
		claims.add(new FactionClaim(chunk));
		updateClaims();
	}
	
	public boolean unclaim(Chunk chunk) {
		boolean b = claims.removeIf(claim -> claim.isChunk(chunk));
		updateClaims();
		return b;
	}
	
	private void updateClaims() {
		try {
			PreparedStatement statement = ((FactionManager) manager).updateFactionClaimsStatement.getStatement();
			statement.setString(1, new Gson().toJson(claims));
			statement.setInt(2, id);
			statement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
			broadcast("Une erreur est survenue.");
		}
	}

	public void updateHome(Location home) {
		this.home = home;
		try {
			PreparedStatement statement = ((FactionManager) manager).updateFactionHomeStatement.getStatement();
			statement.setString(1, SpigotUtils.convertLocationToString(home));
			statement.setInt(2, id);
			statement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
			broadcast("Une erreur est survenue.");
		}
	}
	
	// TODO ROLES
	public Set<FactionPlayer> getOnlinePlayers(OlympaFactionRole officer) {
		return null;
	}
	
	public OlympaFactionRole getRole(Player player) {
		return null;
	}

	public int getMaxPower() {
		return getMembersAmount() * FactionPlayer.POWER_MAX;
	}
	
	public String getClaimsPowerMaxPower() {
		return getClaims().size() + "/" + getPower() + "/" + getMaxPower();
	}
}
