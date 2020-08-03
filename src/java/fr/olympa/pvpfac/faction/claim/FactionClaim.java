package fr.olympa.pvpfac.faction.claim;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;

public class FactionClaim {

	long id;
	World world;
	int x;
	int z;
	Faction faction;
	Set<Integer> ownerIds = new HashSet<>();
	FactionClaimType type;

	public FactionClaim(int id, int world, int x, int z, Integer factionId, int type, String ownerIds) {
		this.id = id;
		this.world = Bukkit.getWorlds().get(world);
		this.x = x;
		this.z = z;
		faction = factionId == null ? null : PvPFaction.getInstance().getFactionManager().getClan(id);
		this.type = FactionClaimType.get(type);
		this.ownerIds = ownerIds == null ? null : Arrays.stream(ownerIds.split(",")).map(Integer::parseInt).collect(Collectors.toSet());
	}

	public FactionClaim(Chunk chunk, Faction faction) {
		world = chunk.getWorld();
		x = chunk.getX();
		z = chunk.getZ();
		this.faction = faction;
		type = FactionClaimType.WILDERNESS;
	}

	public FactionClaim(Chunk chunk, FactionClaimType type) {
		world = chunk.getWorld();
		x = chunk.getX();
		z = chunk.getZ();
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public World getWorld() {
		return world;
	}

	public String getWorldName() {
		return getWorld().getName();
	}

	public boolean isDefaultWorld() {
		return Bukkit.getWorlds().indexOf(world) == 0;
	}

	public long getX() {
		return x;
	}

	public long getZ() {
		return z;
	}

	public boolean isChunk(Chunk chunk) {
		return x == chunk.getX() && z == chunk.getZ() && chunk.getWorld().getName().equals(getWorldName());
	}

	public void setFaction(Faction faction) {
		this.faction = faction;
	}

	public FactionClaimType getType() {
		return type;
	}

	public void setType(FactionClaimType type) {
		this.type = type;
	}

	public Faction getFaction() {
		return faction;
	}

	public Chunk getChunk() {
		return world.getChunkAt(x, z);
	}

	public boolean canInteract(Faction faction) {
		if (this.faction != null)
			return this.faction.isSameClan(faction);
		return type == FactionClaimType.WILDERNESS;
	}

	public boolean isOverClaimable() {
		if (faction != null)
			return faction.isOverClaimable() && PvPFaction.getInstance().getClaimsManager().getChunksAround(getChunk()).stream().anyMatch(c -> !faction.hasClaim(c));
		return type == FactionClaimType.WILDERNESS;
	}

	public void sendTitle(Player player) {
		if (faction != null)
			player.sendTitle(faction.getNameColored(player.getUniqueId()), "§7" + faction.getDescription(), 0, 20, 20);
		else
			player.sendTitle(type.getNameColored(), type.getDescriptionColored(), 0, 20, 20);

	}

	public ChatColor getColor() {
		return type.getColor();
	}

	public Set<Integer> getOwnerIds() {
		return ownerIds;
	}

	public String getFactionNameColored() {
		if (faction != null)
			return ChatColor.DARK_RED + faction.getName();
		return type.getNameColored();
	}

	public boolean hasSameFaction(FactionClaim claim) {
		if (faction != null)
			return claim.getFaction() != null && faction.isSameClan(claim.getFaction());
		return claim.getFaction() == null && getType() == claim.getType();
	}
}
