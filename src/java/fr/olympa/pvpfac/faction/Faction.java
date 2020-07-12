package fr.olympa.pvpfac.faction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.google.gson.Gson;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.claim.FactionClaim;
import fr.olympa.pvpfac.player.FactionPlayer;

public class Faction extends Clan<Faction, FactionPlayerData> {
	
	private static FixedLine<Scoreboard<FactionPlayer>> header = new FixedLine<>("§7§oMa Faction:");
	private static TimerLine<Scoreboard<FactionPlayer>> players = new TimerLine<>((x) -> {
		FactionPlayer fp = x.getOlympaPlayer();
		Faction faction = fp.getClan();
		Player p = fp.getPlayer();
		StringJoiner joiner = new StringJoiner("\n");
		for (FactionPlayerData member : faction.getMembers()) {
			String memberName = member.getPlayerInformations().getName();
			if (member.isConnected())
				joiner.add("§c○ " + memberName);
			else if (member.getConnectedPlayer() == x.getOlympaPlayer())
				joiner.add("§6● §l" + memberName);
			else {
				Location loc = member.getConnectedPlayer().getPlayer().getLocation();
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
	FactionType type;

	public Faction(ClansManager<Faction, FactionPlayerData> manager, int id, String name, OlympaPlayerInformations chief, int maxSize) {
		super(manager, id, name, chief, maxSize);
		type = FactionType.PLAYER;
	}
	
	public Faction(ClansManager<Faction, FactionPlayerData> manager, int id, String name, String description, OlympaPlayerInformations chief, FactionType type) {
		super(manager, id, name, chief, manager.defaultMaxSize);
		this.description = description;
		this.type = type;
	}
	
	public Faction(ClansManager<Faction, FactionPlayerData> manager, int id, String name, OlympaPlayerInformations chief, int maxSize, double money, long created, String tag, String description, Location home, Set<FactionClaim> claims, FactionType type) {
		super(manager, id, name, chief, maxSize, money, created);
		this.tag = tag;
		this.description = description;
		this.home = home;
		this.claims = claims;
		this.type = type;
	}
	
	public FactionType getType() {
		return type;
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
		return members.values().stream().filter(entry -> !entry.isConnected()).map(p -> (FactionPlayer) AccountProvider.get(p.getPlayerInformations().getUUID())).collect(Collectors.toSet());
	}

	public int getPower() {
		return getPlayers().stream().mapToInt(p -> ((FactionPlayer) AccountProvider.get(p.getUniqueId())).getPower()).sum();
	}
	
	public FactionClaim getClaim(Chunk chunk) {
		return claims.stream().filter(claim -> claim.isChunk(chunk)).findFirst().orElse(null);
	}
	
	public boolean hasClaim(Chunk chunk) {
		return claims.stream().anyMatch(claim -> claim.isChunk(chunk));
	}
	
	public boolean isOverClaimable() {
		return claims.size() > getPower() && type == FactionType.PLAYER;
	}
	
	@Override
	public void memberJoin(ClanPlayerInterface<Faction, FactionPlayerData> member) {
		super.memberJoin(member);
		
		Scoreboard<FactionPlayer> scoreboard = PvPFaction.getInstance().scoreboards.getPlayerScoreboard((FactionPlayer) member);
		scoreboard.addLine(FixedLine.EMPTY_LINE);
		scoreboard.addLine(header);
		scoreboard.addLine(players);
	}
	
	@Override
	protected void removedOnlinePlayer(ClanPlayerInterface<Faction, FactionPlayerData> oplayer) {
		super.removedOnlinePlayer(oplayer);

		PvPFaction.getInstance().scoreboards.removePlayerScoreboard((FactionPlayer) oplayer);
		PvPFaction.getInstance().scoreboards.create((FactionPlayer) oplayer);
	}
	
	public void claim(Chunk chunk) throws SQLException {
		claims.add(new FactionClaim(chunk));
		updateClaims();
	}

	public boolean unclaim(Chunk chunk) throws SQLException {
		boolean b = claims.removeIf(claim -> claim.isChunk(chunk));
		updateClaims();
		return b;
	}

	private void updateClaims() throws SQLException {
		PreparedStatement statement = ((FactionManager) manager).updateFactionClaimsStatement.getStatement();
		statement.setString(1, new Gson().toJson(claims));
		statement.setInt(2, id);
		statement.executeUpdate();
	}
	
	public void updateHome(Location home) throws SQLException {
		this.home = home;
		PreparedStatement statement = ((FactionManager) manager).updateFactionHomeStatement.getStatement();
		statement.setString(1, SpigotUtils.convertLocationToString(home));
		statement.setInt(2, id);
		statement.executeUpdate();
	}
	
	public int getMaxPower() {
		return getMembersAmount() * FactionPlayer.POWER_MAX;
	}

	public String getClaimsPowerMaxPower() {
		return getClaims().size() + "/" + getPower() + "/" + getMaxPower();
	}
	
	public boolean updateTag(String tag) throws SQLException {
		if (tag.length() == 1 || tag.length() > 6 || Pattern.compile("[^a-zA-Z]").matcher(tag).find())
			return false;
		this.tag = tag;
		PreparedStatement statement = ((FactionManager) manager).updateTagStatement.getStatement();
		statement.setString(1, tag);
		statement.setInt(2, id);
		statement.executeUpdate();
		return true;
	}
	
	public boolean updateDescription(String description) throws SQLException {
		if (description.length() < 3 || description.length() > 100 || Pattern.compile("[^a-zA-Z]").matcher(description).find())
			return false;
		this.description = description;
		PreparedStatement statement = ((FactionManager) manager).updateDescriptionStatement.getStatement();
		statement.setString(1, description);
		statement.setInt(2, id);
		statement.executeUpdate();
		return true;
	}
	
	public String getNameColored(UUID uuid) {
		return getNameColored(((FactionPlayer) AccountProvider.get(uuid)).getClan());
	}

	public String getNameColored(Faction clan) {
		return (clan != null && clan.getID() == id ? ChatColor.GREEN : ChatColor.RED) + getName();
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
