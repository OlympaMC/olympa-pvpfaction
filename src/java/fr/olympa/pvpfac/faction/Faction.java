package fr.olympa.pvpfac.faction;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.lines.DynamicLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.claim.FactionClaim;
import fr.olympa.pvpfac.faction.claim.FactionClaimsManager;
import fr.olympa.pvpfac.player.FactionPlayer;

public class Faction extends Clan<Faction, FactionPlayerData> {

	private static DynamicLine<Scoreboard<FactionPlayer>> header = new DynamicLine<>(x -> "§7" + x.getOlympaPlayer().getClan().getName() + ":");
	private static TimerLine<Scoreboard<FactionPlayer>> players = new TimerLine<>((x) -> {
		Faction fac = x.getOlympaPlayer().getClan();
		Player p = x.getOlympaPlayer().getPlayer();
		Map<String, Integer> players = new HashMap<>();
		for (FactionPlayerData member : fac.getMembers()) {
			String memberName = member.getPlayerInformations().getName();
			if (!member.isConnected())
				players.put("§c○ " + memberName, 3);
			else if (member.getConnectedPlayer() == x.getOlympaPlayer())
				players.put("§6● §l" + memberName, 1);
			else {
				Location loc = member.getConnectedPlayer().getPlayer().getLocation();
				players.put("§e● " + memberName + " §l" + SpigotUtils.getDirectionToLocation(p, loc), 2);
			}
		}
		return players.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Entry::getKey).collect(Collectors.joining("\n"));
	}, PvPFaction.getInstance(), 10);

	String tag;
	String description;
	Location home;

	public Faction(ClansManager<Faction, FactionPlayerData> manager, int id, String name, OlympaPlayerInformations chief, int maxSize) {
		super(manager, id, name, chief, maxSize);
	}

	public Faction(ClansManager<Faction, FactionPlayerData> manager, int id, String name, String description, OlympaPlayerInformations chief) {
		super(manager, id, name, chief, manager.defaultMaxSize);
		this.description = description;
	}

	public Faction(ClansManager<Faction, FactionPlayerData> manager, int id, String name, OlympaPlayerInformations chief, int maxSize, double money, long created, String tag, String description, Location home) {
		super(manager, id, name, chief, maxSize, money, created);
		this.tag = tag;
		this.description = description;
		this.home = home;
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
		return members.values().stream()
				.filter(entry -> !entry.isConnected())
				.map(p -> (FactionPlayer) AccountProvider.get(p.getPlayerInformations().getUUID()))
				.collect(Collectors.toSet());
	}

	public int getPower() {
		return getPlayers().stream().mapToInt(p -> ((FactionPlayer) AccountProvider.get(p.getUniqueId())).getPower()).sum();
	}

	// TODO Optimize getByFaction
	public Set<FactionClaim> getClaims() {
		return PvPFaction.getInstance().getClaimsManager().getByFaction(this);
	}

	public boolean hasClaim(Chunk chunk) {
		return hasClaim(PvPFaction.getInstance().getClaimsManager().getByChunk(chunk));
	}

	public boolean hasClaim(FactionClaim chunk) {
		Faction faction = chunk.getFaction();
		return faction != null && faction.getID() == getID();
	}

	public boolean isOverClaimable() {
		return getClaims().size() > getPower();
	}

	public FactionManager getFactionManager() {
		return getClansManager();
	}

	@SuppressWarnings("unchecked")
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

	public void claim(FactionClaim factionClaim) throws SQLException {
		FactionClaimsManager claimManager = PvPFaction.getInstance().getClaimsManager();
		factionClaim.setFaction(this);
		claimManager.updateClaim(factionClaim);
	}

	public void unclaim(FactionClaim factionClaim) throws SQLException {
		FactionClaimsManager claimManager = PvPFaction.getInstance().getClaimsManager();
		factionClaim.setFaction(null);
		claimManager.updateClaim(factionClaim);
	}

	public void updateHome(Location home) throws SQLException {
		getFactionManager().homeColumn.updateValue(this, SpigotUtils.convertLocationToString(home), Types.VARCHAR);
		this.home = home;
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
		getFactionManager().tagColumn.updateValue(this, tag, Types.VARCHAR);
		this.tag = tag;
		return true;
	}

	public boolean updateDescription(String description) throws SQLException {
		if (description.length() < 3 || description.length() > 100 || Pattern.compile("[^a-zA-Z]").matcher(description).find())
			return false;
		getFactionManager().descriptionColumn.updateValue(this, description, Types.VARCHAR);
		this.description = description;
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

	@Override
	public Faction clone() {
		try {
			return (Faction) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

}
