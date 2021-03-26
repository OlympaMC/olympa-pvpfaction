package fr.olympa.pvpfac.faction.claim;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.faction.FactionPlayerData.FactionRole;
import fr.olympa.pvpfac.player.FactionPlayer;

public class FactionClaim {
	
	//public static final FactionClaim WILDERNESS = new FactionClaim(null, Integer.MAX_VALUE, Integer.MAX_VALUE, null, null, null);//new WildernessFactionClaim();
	
	//private ChunkId chunkId;
	private int id;
	private Faction faction;
	private Map<Long, ClaimPermLevel> membersPlayers = new HashMap<Long, ClaimPermLevel>();
	private Map<Integer, ClaimPermLevel[]> membersFactions = new HashMap<Integer, ClaimPermLevel[]>();

	public FactionClaim(int id, Integer factionId, String playersMembersAsJson, String factionsMembersAsJson) {
		this.id = id;
		
		faction = factionId == null ? null : PvPFaction.getInstance().getFactionManager().getClan(factionId);
		//this.type = FactionClaimType.get(type);
		this.membersPlayers = playersMembersAsJson == null || playersMembersAsJson.length() <= 2? new HashMap<Long, ClaimPermLevel>() : 
			((Map<Long, Integer>)new Gson().fromJson(playersMembersAsJson, new TypeToken<Map<Long, Integer>>(){}.getType())).entrySet()
			.stream().map(e -> new AbstractMap.SimpleEntry<Long, ClaimPermLevel>(e.getKey(), ClaimPermLevel.fromLevel(e.getValue())))
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		
		this.membersFactions = factionsMembersAsJson == null || factionsMembersAsJson.length() <= 2 ? new HashMap<Integer, ClaimPermLevel[]>() : 
			((Map<Integer, Integer[]>)new Gson().fromJson(playersMembersAsJson, new TypeToken<Map<Integer, Integer[]>>(){}.getType())).entrySet()
			.stream().map(e -> new AbstractMap.SimpleEntry<Integer, ClaimPermLevel[]>(e.getKey(), 
					(ClaimPermLevel[]) Stream.of(e.getValue()).map(perm -> ClaimPermLevel.fromLevel(perm)).toArray()))
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}

	/*
	public ChunkId getChunkId() {
		return chunkId;
	}

	@SuppressWarnings("unlikely-arg-type")
	public boolean isChunk(Chunk chunk) {
		return chunkId.equals(chunk);
	}*/
	
	public int getId() {
		return id;
	}
	
	public boolean setFaction(Faction faction) {		
		if ((faction == null && getFaction() == null) || faction.equals(getFaction()))
			return false;
		
		this.faction = faction;
		membersPlayers.clear();
		membersFactions.clear();
		
		PvPFaction.getInstance().getClaimsManager().updateClaim(this);
		
		return true;
	}

	public Faction getFaction() {
		return faction;
	}

	public boolean isOverClaimable() {
		if (faction == null)
			return true;
		return faction.isOverClaimable();// && PvPFaction.getInstance().getClaimsManager().getChunksAround(chunkId.getChunk()).stream().anyMatch(c -> !faction.hasClaim(c));
	}

	public void sendTitle(Player player) {
		if (faction != null)
			player.sendTitle(faction.getNameColored(player.getUniqueId()), "§7" + faction.getDescription(), 0, 20, 20);
		else
			player.sendTitle("§2Wilderness", "§aZone sans aucune restriction", 0, 20, 20);

	}

	public String getFactionNameColored() {
		if (faction != null)
			return ChatColor.DARK_RED + faction.getName();
		return "§6Wilderness";
	}

	public boolean hasSameFaction(FactionClaim claim) {
		return faction == null ?
				claim.getFaction() == null :
				faction.getID() == claim.getFaction().getID();
		/*if (faction != null)
			return claim.getFaction() != null && faction.isSameClan(claim.getFaction());
		return claim.getFaction() == null;// && getType() == claim.getType();*/
	}
	
	//for database saving only
	public String getPlayersMembersToJson() {PvPFaction.getInstance().getFactionManager().getClan(1);
		return membersPlayers.size() == 0 ? null : new Gson().toJson(membersPlayers.entrySet().stream()
				.map(e -> new AbstractMap.SimpleEntry<Long, Integer>(e.getKey(), e.getValue().getLevel()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())), new TypeToken<Map<Long, Integer>>(){}.getType());
	}
	
	//for database saving only
	public String getFactionMembersToJson() {
		return membersFactions.size() == 0 ? null : new Gson().toJson(membersFactions.entrySet().stream()
				.map(e -> new AbstractMap.SimpleEntry<Integer, Integer[]>(e.getKey(), (Integer[]) Stream.of(e.getValue()).map(perm -> perm.getLevel()).toArray()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())), new TypeToken<Map<Integer, Integer[]>>(){}.getType());
	}
	
	public boolean setPlayerLevel(FactionPlayer pf, ClaimPermLevel level) {
		if (membersPlayers.containsKey(pf.getId()) && membersPlayers.get(pf.getId()) == level)
			return false;

		/*if (level == ClaimPermLevel.LEVEL_NONE)
			claimMembersPlayers.remove(pf.getId());
		else*/
			membersPlayers.put(pf.getId(), level);
			
		PvPFaction.getInstance().getClaimsManager().updateClaim(this);
		return true;
	}
	
	public boolean setAllyFactionLevel(FactionRole role, ClaimPermLevel level) {
		return setFactionLevel(null, role, level);
	}
	
	public boolean setFactionLevel(Faction f, FactionRole role, ClaimPermLevel level) {
		//faction index -1 is used for allied factions
		int factionId = f == null ? -1 : f.getID();
		
		if ((!membersFactions.containsKey(factionId) && level == ClaimPermLevel.LEVEL_NONE) || 
				(membersFactions.containsKey(factionId) && membersFactions.get(f.getID())[role.weight] == level))
			return false;

		//on ajoute les permissions par défaut à tous les grades de la faction elle n'est pas encore dans la liste
		if (!membersFactions.containsKey(factionId)) {
			membersFactions.put(factionId, new ClaimPermLevel[FactionRole.LEADER.weight + 1]);
			for (int i = 0 ; i <= FactionRole.LEADER.weight ; i++)
				membersFactions.get(factionId)[i] = ClaimPermLevel.LEVEL_NONE;
		}
		
		ClaimPermLevel[] perms = membersFactions.get(factionId);
		perms[role.weight] = level;
		
		//fais en sorte que les roles inférieurs n'ai pas plus de permission que le role en cours d'édition, et vice versa
		for (int i = 0 ; i <= FactionRole.RECRUT.weight ; i++)
			if ((perms[i].getLevel() > level.getLevel() && i < role.weight) || (perms[i].getLevel() < level.getLevel() && i > role.weight))
				perms[i] = level;
		
		//si aucun grade dans la fac n'a de permission, on retire la fac de la liste
		if (Stream.of(membersFactions.get(factionId)).filter(perm -> perm != ClaimPermLevel.LEVEL_NONE).findFirst().isEmpty())
			membersFactions.remove(factionId);
		
		PvPFaction.getInstance().getClaimsManager().updateClaim(this);
		return true;
	}
	
	
	public ClaimPermLevel getPlayerPerm(FactionPlayer pf) {
		if (faction == null)
			return ClaimPermLevel.LEVEL_4;
		else
			return membersPlayers.size() > 0 ? 
					membersPlayers.containsKey(pf.getId()) ?
							membersPlayers.get(pf.getId()) :
							ClaimPermLevel.LEVEL_NONE :
					pf.getClan() == null ?
							ClaimPermLevel.LEVEL_NONE :
							pf.getClan().equals(faction) ?
									faction.getMember(pf.getInformation()).getRole().getPlayerClaimLevel() :
									membersFactions.containsKey(-1) && pf.getClan().isAlly(faction) ? 
											membersFactions.get(-1)[pf.getClan().getMember(pf.getInformation()).getRole().weight] :
											membersFactions.containsKey(pf.getClan().getID()) ?
													membersFactions.get(pf.getClan().getID())[pf.getClan().getMember(pf.getInformation()).getRole().weight] :
													ClaimPermLevel.LEVEL_NONE;
	}
	
	/*private static class WildernessFactionClaim extends FactionClaim {

		public WildernessFactionClaim() {
			super(null, Integer.MAX_VALUE, Integer.MAX_VALUE, null, null);
		}	
	}*/
	
	public static final class ChunkId {

		//private World w;
		private int x;
		private int z;
		
		public ChunkId(int x, int z) {
			//this.w = w;
			this.x = x;
			this.z = z;
		}
		
		public ChunkId(Chunk ch) {
			this(ch.getX(), ch.getZ());
		}
		
		/*
		public World getWorld() {
			return w;
		}*/
		
		public int getX() {
			return x;
		}
		
		public int getZ() {
			return z;
		}

		/**
		 * Get chunk of this FactionChunk. Avoid usnig this method since it might load chunk synchronously to the main thread.
		 * @return
		 */
		/*
		@Deprecated
		public Chunk getChunk() {
			return w.getChunkAt(x, z);
		}
		
		public void getChunk(Consumer<Chunk> callback) {
			w.getChunkAtAsync(x, z, callback);
		}*/
		
		@Override
		public boolean equals(Object o) {
			return o instanceof ChunkId ? ((ChunkId)o).x == this.x && ((ChunkId)o).z == this.z :
				o instanceof Chunk ? ((Chunk)o).getX() == this.x && ((Chunk)o).getZ() == this.z :
					false;
		}
	}
}
