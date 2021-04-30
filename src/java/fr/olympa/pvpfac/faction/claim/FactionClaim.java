package fr.olympa.pvpfac.faction.claim;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.player.FactionPlayer;
import fr.olympa.pvpfac.player.FactionPlayerData.FactionRole;

public class FactionClaim {
	
	private ClaimId claimId;
	private Faction faction;
	private FactionClaimType type;
	private Map<Long, FactionClaimPermLevel> membersPlayers = new HashMap<Long, FactionClaimPermLevel>();
	private Map<Integer, FactionClaimPermLevel[]> membersFactions = new HashMap<Integer, FactionClaimPermLevel[]>();

	public FactionClaim(ClaimId id, FactionClaimType type, Integer factionId, String playersMembersAsJson, String factionsMembersAsJson) {
		this.claimId = id;
		
		this.type = type;
		
		faction = factionId == null ? null : PvPFaction.getInstance().getFactionManager().getClan(factionId);

		this.membersPlayers = playersMembersAsJson == null || playersMembersAsJson.length() <= 2? new HashMap<Long, FactionClaimPermLevel>() : 
			((Map<Long, Integer>)new Gson().fromJson(playersMembersAsJson, new TypeToken<Map<Long, Integer>>(){}.getType())).entrySet()
			.stream().map(e -> new AbstractMap.SimpleEntry<Long, FactionClaimPermLevel>(e.getKey(), FactionClaimPermLevel.fromLevel(e.getValue())))
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		
		this.membersFactions = factionsMembersAsJson == null || factionsMembersAsJson.length() <= 2 ? new HashMap<Integer, FactionClaimPermLevel[]>() : 
			((Map<Integer, Integer[]>)new Gson().fromJson(playersMembersAsJson, new TypeToken<Map<Integer, Integer[]>>(){}.getType())).entrySet()
			.stream().map(e -> new AbstractMap.SimpleEntry<Integer, FactionClaimPermLevel[]>(e.getKey(), 
					(FactionClaimPermLevel[]) Stream.of(e.getValue()).map(perm -> FactionClaimPermLevel.fromLevel(perm)).toArray()))
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}
	
	public void setFaction(Faction faction) {
		if (!type.isClaimable())
			return;
		
		if ((faction == null && getFaction() == null) || faction == null ? getFaction().equals(faction) : faction.equals(getFaction()))
			return;
		
		this.faction = faction;
		membersPlayers.clear();
		membersFactions.clear();
		
		PvPFaction.getInstance().getClaimsManager().updateClaim(this);
	}
	
	public void setType(FactionClaimType type) {
		if (this.type == type)
			return;

		this.faction = null;
		membersPlayers.clear();
		membersFactions.clear();
		
		this.type = type;
		
		PvPFaction.getInstance().getClaimsManager().updateClaim(this);
	}

	public Faction getFaction() {
		return faction;
	}

	public FactionClaimType getType() {
		return type;
	}
	
	public ClaimId getClaimId() {
		return claimId;
	}

	public boolean isOverClaimable() {
		if (!type.isClaimable())
			return false;
		else if (faction == null)
			return true;
		else
			return faction.isOverClaimable();
	}

	public void sendTitle(Player player) {
		if (faction != null)
			player.sendTitle(faction.getNameColored(player.getUniqueId()), "§7" + faction.getDescription(), 0, 20, 20);
		else
			type.sendTitle(player);

	}

	public boolean hasSameFaction(FactionClaim claim) {
		return faction == null ?
			   claim.getFaction() == null :
					claim.getFaction() == null ? 
						false :	faction.getID() == claim.getFaction().getID();
	}
	
	//for database saving only
	public String getPlayersMembersAsJson() {
		return membersPlayers.size() == 0 ? null : new Gson().toJson(membersPlayers.entrySet().stream()
				.map(e -> new AbstractMap.SimpleEntry<Long, Integer>(e.getKey(), e.getValue().getLevel()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));//, new TypeToken<Map<Long, Integer>>(){}.getType());
	}
	
	//for database saving only
	public String getFactionMembersAsJson() {
		return membersFactions.size() == 0 ? null : new Gson().toJson(membersFactions.entrySet().stream()
				.map(e -> new AbstractMap.SimpleEntry<Integer, Integer[]>(e.getKey(), (Integer[]) Stream.of(e.getValue()).map(perm -> perm.getLevel()).toArray(i -> new Integer[i])))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
	}
	
	public boolean setPlayerLevel(FactionPlayer pf, FactionClaimPermLevel level) {
		if (membersPlayers.containsKey(pf.getId()) ? membersPlayers.get(pf.getId()) == level : level == FactionClaimPermLevel.NONE)
			return false;
		
		if (level ==FactionClaimPermLevel.NONE)
			membersPlayers.remove(pf.getId());
		else
			membersPlayers.put(pf.getId(), level);
			
		PvPFaction.getInstance().getClaimsManager().updateClaim(this);
		return true;
	}
	
	public boolean setFactionLevel(Faction f, FactionRole role, FactionClaimPermLevel level) {
		if (membersFactions.containsKey(f.getID()) ? membersFactions.get(f.getID())[role.weight] == level : level == FactionClaimPermLevel.NONE)
			return false;

		//on ajoute les permissions par défaut à tous les grades de la faction elle n'est pas encore dans la liste
		if (!membersFactions.containsKey(f.getID())) {
			membersFactions.put(f.getID(), new FactionClaimPermLevel[FactionRole.LEADER.weight + 1]);
			for (int i = 0 ; i <= FactionRole.LEADER.weight ; i++)
				membersFactions.get(f.getID())[i] = FactionClaimPermLevel.NONE;
		}
		
		FactionClaimPermLevel[] perms = membersFactions.get(f.getID());
		perms[role.weight] = level;
		
		//fais en sorte que les roles inférieurs n'ai pas plus de permission que le role en cours d'édition, et vice versa
		for (int i = 0 ; i <= FactionRole.RECRUT.weight ; i++)
			if ((perms[i].getLevel() > level.getLevel() && i < role.weight) || (perms[i].getLevel() < level.getLevel() && i > role.weight))
				perms[i] = level;
		
		//si aucun grade dans la fac n'a de permission, on retire la fac de la liste
		if (Stream.of(membersFactions.get(f.getID())).filter(perm -> perm != FactionClaimPermLevel.NONE).findFirst().isEmpty())
			membersFactions.remove(f.getID());
		
		PvPFaction.getInstance().getClaimsManager().updateClaim(this);
		return true;
	}
	
	
	public FactionClaimPermLevel getPlayerPerm(FactionPlayer pf) {
		if (!type.isClaimable())
			return FactionClaimPermLevel.LVL_1;
		
		else if (faction == null)
			return FactionClaimPermLevel.LVL_4;
		
		else if (pf.getClan() != null && membersFactions.containsKey(pf.getClan().getID()))
			return membersFactions.get(pf.getClan().getID()) [pf.getClan().getMember(pf.getInformation()).getRole().weight];
		
		else if (membersPlayers.size() > 0)
			return membersPlayers.containsKey(pf.getId()) ? membersPlayers.get(pf.getId()) : FactionClaimPermLevel.NONE;
			
		else if (pf.getClan() != null && pf.getClan().equals(faction))
			return faction.getMember(pf.getInformation()).getRole().getDefaultClaimLevel();
		
		else
			return FactionClaimPermLevel.NONE;
	}
	
	
	@Override
	public String toString() {
		return claimId.toString();
	}


	
	
	public static final class ClaimId {

		private long id;
		private int x;
		private int z;
		
		public ClaimId(long id, int x, int z) {
			this.id = id;
			this.x = x;
			this.z = z;
		}
		
		public ClaimId(long id, Chunk ch) {
			this(id, ch.getX(), ch.getZ());
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
		
		@Override
		public boolean equals(Object o) {
			return o instanceof ClaimId ? ((ClaimId)o).id == this.id :
				o instanceof Chunk ? ((Chunk)o).getX() == this.x && ((Chunk)o).getZ() == this.z :
				false;
		}
		
		@Override
		public String toString() {
			return "[claim " + id + ": x=" + x + ", z=" + z + "]";
		}
	}
}
