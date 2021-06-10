package fr.olympa.pvpfac.faction.claim;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.player.FactionPlayer;
import fr.olympa.pvpfac.player.FactionPlayerData.FactionRole;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FactionClaim {

	private final ClaimId claimId;
	private final Map<Long, FactionClaimPermLevel> membersPlayers;
	private final Map<Integer, FactionClaimPermLevel[]> membersFactions;
	private Faction faction;
	private FactionClaimType type;

	public FactionClaim(final ClaimId id, final FactionClaimType type, final Integer factionId, final String playersMembersAsJson, final String factionsMembersAsJson) {
		this.claimId = id;

		this.type = type;

		faction = factionId == null ? null : PvPFaction.getInstance().getFactionManager().getClan(factionId);

		this.membersPlayers = playersMembersAsJson == null || playersMembersAsJson.length() <= 2 ? new HashMap<>() :
		                      ((Map<Long, Integer>) new Gson().fromJson(playersMembersAsJson, new TypeToken<Map<Long, Integer>>() {}.getType())).entrySet()
			                      .stream().map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), FactionClaimPermLevel.fromLevel(e.getValue())))
			                      .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

		this.membersFactions = factionsMembersAsJson == null || factionsMembersAsJson.length() <= 2 ? new HashMap<>() :
		                       ((Map<Integer, Integer[]>) new Gson().fromJson(playersMembersAsJson, new TypeToken<Map<Integer, Integer[]>>() {}.getType())).entrySet()
			                       .stream().map(e -> new AbstractMap.SimpleEntry<>(
			                       e.getKey(),
			                       (FactionClaimPermLevel[]) Stream.of(e.getValue()).map(FactionClaimPermLevel::fromLevel).toArray()
		                       ))
			                       .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
	}

	public void sendTitle(final Player player) {
		if (faction != null) {
			player.sendTitle(faction.getNameColored(player.getUniqueId()), "§7" + faction.getDescription(), 0, 20, 20);
		} else {
			type.sendTitle(player);
		}

	}

	public boolean hasSameFaction(final FactionClaim claim) {
		return faction == null ?
		       claim.getFaction() == null :
		       claim.getFaction() != null && faction.getID() == claim.getFaction().getID();
	}

	public Faction getFaction() {
		return faction;
	}

	public void setFaction(final Faction faction) {
		if (!type.isClaimable()) {
			return;
		}

		if (faction == null ? getFaction().equals(faction) : faction.equals(getFaction())) {
			return;
		}

		this.faction = faction;
		membersPlayers.clear();
		membersFactions.clear();

		PvPFaction.getInstance().getClaimsManager().updateClaim(this);
	}

	public boolean setPlayerLevel(final FactionPlayer pf, final FactionClaimPermLevel level) {
		if (membersPlayers.containsKey(pf.getId()) ? membersPlayers.get(pf.getId()) == level : level == FactionClaimPermLevel.NONE) return false;

		if (level == FactionClaimPermLevel.NONE) {
			membersPlayers.remove(pf.getId());
		} else {
			membersPlayers.put(pf.getId(), level);
		}

		PvPFaction.getInstance().getClaimsManager().updateClaim(this);
		return true;
	}

	public boolean setFactionLevel(final Faction f, final FactionRole role, final FactionClaimPermLevel level) {
		if (membersFactions.containsKey(f.getID()) ? membersFactions.get(f.getID())[role.weight] == level : level == FactionClaimPermLevel.NONE) return false;

		//on ajoute les permissions par défaut à tous les grades de la faction elle n'est pas encore dans la liste
		if (!membersFactions.containsKey(f.getID())) {
			membersFactions.put(f.getID(), new FactionClaimPermLevel[FactionRole.LEADER.weight + 1]);
			for (int i = 0; i <= FactionRole.LEADER.weight; i++) {
				membersFactions.get(f.getID())[i] = FactionClaimPermLevel.NONE;
			}
		}

		final FactionClaimPermLevel[] perms = membersFactions.get(f.getID());
		perms[role.weight] = level;

		//fais en sorte que les rôles inférieurs n'aient pas plus de permission que le rôle en cours d'édition, et vice-versa
		for (int i = 0; i <= FactionRole.RECRUT.weight; i++) {
			if ((perms[i].getLevel() > level.getLevel() && i < role.weight) || (perms[i].getLevel() < level.getLevel() && i > role.weight)) {
				perms[i] = level;
			}
		}

		//si aucun grade dans la fac n'a de permission, on retire la fac de la liste
		if (Stream.of(membersFactions.get(f.getID())).filter(perm -> perm != FactionClaimPermLevel.NONE).findFirst().isEmpty()) {
			membersFactions.remove(f.getID());
		}

		PvPFaction.getInstance().getClaimsManager().updateClaim(this);
		return true;
	}

	public FactionClaimPermLevel getPlayerPerm(final FactionPlayer pf) {
		if (!type.isClaimable()) {
			return FactionClaimPermLevel.LVL_1;
		} else if (faction == null) {
			return FactionClaimPermLevel.LVL_4;
		} else if (pf.getClan() != null && membersFactions.containsKey(pf.getClan().getID())) {
			return membersFactions.get(pf.getClan().getID())[pf.getClan().getMember(pf.getInformation()).getRole().weight];
		} else if (!membersPlayers.isEmpty()) {
			return membersPlayers.getOrDefault(pf.getId(), FactionClaimPermLevel.NONE);
		} else if (pf.getClan() != null && pf.getClan().equals(faction)) {
			return faction.getMember(pf.getInformation()).getRole().getDefaultClaimLevel();
		} else {
			return FactionClaimPermLevel.NONE;
		}
	}

	@Override
	public String toString() {
		return claimId.toString();
	}

	public static final class ClaimId {

		private final long id;
		private final int x;
		private final int z;

		public ClaimId(final long id, final Chunk ch) {
			this(id, ch.getX(), ch.getZ());
		}

		public ClaimId(final long id, final int x, final int z) {
			this.id = id;
			this.x = x;
			this.z = z;
		}

		@Override
		public boolean equals(final Object o) {
			return o instanceof ClaimId ? ((ClaimId) o).id == this.id :
			       o instanceof Chunk && ((Chunk) o).getX() == this.x && ((Chunk) o).getZ() == this.z;
		}

		@Override
		public String toString() {
			return "[claim " + id + ": x=" + x + ", z=" + z + "]";
		}

		public long getId() {
			return id;
		}

		public int getX() {
			return x;
		}

		public int getZ() {
			return z;
		}
	}

	public ClaimId getClaimId() {
		return claimId;
	}

	//for database saving only
	public @Nullable String getFactionMembersAsJson() {
		return membersFactions.isEmpty() ? null : new Gson().toJson(
			membersFactions.entrySet().stream()
				.map(e -> new AbstractMap.SimpleEntry<>(
					e.getKey(),
					Stream.of(e.getValue())
						.map(FactionClaimPermLevel::getLevel)
						.toArray(Integer[]::new)
				))
				.collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue))
		);
	}

	//for database saving only
	public @Nullable String getPlayersMembersAsJson() {
		return membersPlayers.isEmpty() ? null : new Gson().toJson(
			membersPlayers.entrySet().stream()
				.map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getLevel()))
				.collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));//, new TypeToken<Map<Long, Integer>>(){}.getType());
	}

	public FactionClaimType getType() {
		return type;
	}

	public void setType(final FactionClaimType type) {
		if (this.type == type) {
			return;
		}

		this.faction = null;
		membersPlayers.clear();
		membersFactions.clear();

		this.type = type;

		PvPFaction.getInstance().getClaimsManager().updateClaim(this);
	}

	public boolean isOverClaimable() {
		return type.isClaimable() && (faction == null || faction.isOverClaimable());
	}

}
