package fr.olympa.pvpfac.faction;

import fr.olympa.api.spigot.clans.Clan;
import fr.olympa.api.spigot.clans.ClanPlayerInterface;
import fr.olympa.api.spigot.clans.ClansManager;
import fr.olympa.api.spigot.lines.DynamicLine;
import fr.olympa.api.spigot.lines.FixedLine;
import fr.olympa.api.spigot.lines.TimerLine;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.spigot.scoreboard.sign.Scoreboard;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.claim.FactionClaim;
import fr.olympa.pvpfac.faction.claim.FactionClaimsManager;
import fr.olympa.pvpfac.player.FactionPlayer;
import fr.olympa.pvpfac.player.FactionPlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Faction extends Clan<Faction, FactionPlayerData> {

	private static final DynamicLine<Scoreboard<FactionPlayer>> header = new DynamicLine<>(x -> "§7" + x.getOlympaPlayer().getClan().getName() + " :");
	private static final TimerLine<Scoreboard<FactionPlayer>> players = new TimerLine<>((x) -> {
		Faction fac = x.getOlympaPlayer().getClan();
		Player p = x.getOlympaPlayer().getPlayer();
		Map<String, Integer> players = new HashMap<>();
		for (FactionPlayerData member : fac.getMembers()) {
			String memberName = member.getPlayerInformations().getName();
			if (!member.isConnected()) {
				players.put("§c○ " + memberName, 3);
			} else if (member.getConnectedPlayer() == x.getOlympaPlayer()) {
				players.put("§6● §l" + memberName, 1);
			} else {
				Location loc = member.getConnectedPlayer().getPlayer().getLocation();
				players.put("§e● " + memberName + " §l" + SpigotUtils.getDirectionToLocation(p, loc), 2);
			}
		}
		return players.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Entry::getKey).collect(Collectors.joining("\n"));
	}, PvPFaction.getInstance(), 10);

	String tag;
	String description;
	Location home;

	public Faction(final ClansManager<Faction, FactionPlayerData> manager, final int id, final String name, final String tag, final OlympaPlayerInformations chief, final int maxSize) {
		super(manager, id, name, tag, chief, maxSize);
	}

	public Faction(final ClansManager<Faction, FactionPlayerData> manager, final int id, final String name, final OlympaPlayerInformations chief, final int maxSize, final double money, final long created, final String tag, final String description, final Location home) {
		super(manager, id, name, tag, chief, maxSize, money, created);
		this.tag = tag;
		this.description = description;
		this.home = home;
	}

	public boolean hasClaim(final Chunk chunk) {
		return hasClaim(PvPFaction.getInstance().getClaimsManager().ofChunk(chunk));
	}

	public boolean hasClaim(final FactionClaim chunk) {
		final Faction faction = chunk.getFaction();
		return faction != null && faction.getID() == getID();
	}

	public void claim(final FactionClaim factionClaim) throws SQLException {
		final FactionClaimsManager claimManager = PvPFaction.getInstance().getClaimsManager();
		factionClaim.setFaction(this);
		claimManager.updateClaim(factionClaim);
	}

	public void unclaim(final FactionClaim factionClaim) throws SQLException {
		final FactionClaimsManager claimManager = PvPFaction.getInstance().getClaimsManager();
		factionClaim.setFaction(null);
		claimManager.updateClaim(factionClaim);
	}

	public void updateHome(final Location home) {
		getFactionManager().homeColumn.updateAsync(this, SpigotUtils.convertLocationToString(home), null, null);
		this.home = home;
	}

	public FactionManager getFactionManager() {
		return getClansManager();
	}

	public boolean updateTag(final String tag) {
		if (tag.length() == 1 || tag.length() > 6 || Pattern.compile("[^a-zA-Z]").matcher(tag).find()) {
			return false;
		}
		getFactionManager().tagColumn.updateAsync(this, tag, null, null);
		this.tag = tag;
		return true;
	}

	public boolean updateDescription(final String description) {
		if (description.length() < 3 || description.length() > 100 || Pattern.compile("[^a-zA-Z]").matcher(description).find()) {
			return false;
		}
		getFactionManager().descriptionColumn.updateAsync(this, description, null, null);
		this.description = description;
		return true;
	}

	public String getNameColored(final UUID uuid) {
		return getNameColored(((FactionPlayer) AccountProvider.getter().get(uuid)).getClan());
	}

	public String getNameColored(final Faction clan) {
		return (clan != null && clan.getID() == id ? ChatColor.GREEN : ChatColor.RED) + getName();
	}

	/**
	 * Return true if fac is an ally or if same fac as this instance is used as parameter
	 * **Not implemented for now.**
	 *
	 * @param fac - The faction to test with.
	 *
	 * @return True if the faction is an ally.
	 *
	 */
	public boolean isAlly(final Faction fac) {
		throw new UnsupportedOperationException("Gestion des factions alliées non encore implémentée !");
		//return false;//TODO
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof Faction && ((Faction) o).getID() == getID();
	}

	@Override
	public @Nullable Faction clone() {
		try {
			return (Faction) super.clone();
		} catch (final CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getClaimsPowerMaxPower() {
		return getClaims().size() + "/" + getPower() + "/" + getMaxPower();
	}

	// TODO Optimize getByFaction
	public Set<FactionClaim> getClaims() {
		return PvPFaction.getInstance().getClaimsManager().ofFaction(this);
	}

	public int getPower() {
		return getPlayers().stream().mapToInt(p -> ((FactionPlayer) AccountProvider.getter().get(p.getUniqueId())).getPower()).sum();
	}

	public int getMaxPower() {
		return getMembersAmount() * FactionPlayer.POWER_MAX;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public Location getHome() {
		return home;
	}

	public Set<FactionPlayer> getOfflineFactionPlayers() {
		return members.values().stream()
			.filter(entry -> !entry.isConnected())
			.map(p -> (FactionPlayer) AccountProvider.getter().get(p.getPlayerInformations().getUUID()))
			.collect(Collectors.toSet());
	}

	public Set<FactionPlayer> getOnlineFactionPlayers() {
		return getPlayers().stream().map(p -> (FactionPlayer) AccountProvider.getter().get(p.getUniqueId())).collect(Collectors.toSet());
	}

	@Override
	public String getTag() {
		return tag;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void memberJoin(final ClanPlayerInterface<Faction, FactionPlayerData> member) {
		super.memberJoin(member);

		final Scoreboard<FactionPlayer> scoreboard = PvPFaction.getInstance().scoreboards.getPlayerScoreboard((FactionPlayer) member);
		scoreboard.addLine(FixedLine.EMPTY_LINE);
		scoreboard.addLine(header);
		scoreboard.addLine(players);
	}

	@Override
	protected void removedOnlinePlayer(final ClanPlayerInterface<Faction, FactionPlayerData> clanPlayer) {
		super.removedOnlinePlayer(clanPlayer);

		PvPFaction.getInstance().scoreboards.refresh((FactionPlayer) clanPlayer);
	}

	@Override
	public void setTag(final String tag) {
		this.tag = tag;
	}

	public boolean isOverClaimable() {
		return getClaims().size() > getPower();
	}

}
