package fr.olympa.pvpfac.factionold.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;

public class OlympaFaction {

	public static boolean isValidName(String name) {
		return !Pattern.compile("^-?\\d+$").matcher(name).find();
	}

	int id;
	String name;
	List<String> oldName = new ArrayList<>();
	String tag;
	String description;
	Set<Player> connected = new HashSet<>();
	Map<UUID, OlympaFactionRole> members = new HashMap<>();
	Set<Chunk> chunks = new HashSet<>();
	Location home;
	long created;

	public OlympaFaction(String name, String tag, Player author) {
		this.name = name;
		this.tag = tag;
		members.put(author.getUniqueId(), OlympaFactionRole.LEADER);
	}

	public void addConnected(Player player) {
		getOnlinePlayers().forEach(p -> p.sendMessage(Prefix.FACTION + "&2" + player.getName() + "&a s'est connecté."));
		connected.add(player);
	}

	public void addOldName(String oldName) {
		this.oldName.add(oldName);
	}

	public void cancel(Player player) {
		getOnlinePlayers().stream().filter(p -> !p.getUniqueId().equals(player.getUniqueId())).forEach(p -> p.sendMessage(Prefix.FACTION + "&4" + player.getName() + "&c a annuler l'invitation."));
	}

	public void claim(Chunk chunk) {
		chunks.add(chunk);
	}

	public void claim(Location location) {
		claim(location.getChunk());
	}

	public OlympaFactionRole demote(Player player) {
		OlympaFactionRole role = getRole(player).getLower();
		if (role != null) {
			setRole(player, role);
		}
		return role;
	}

	public Set<Chunk> getChunks() {
		return chunks;
	}

	private int getClaims() {
		return chunks.size();
	}

	public String getDescription() {
		return description;
	}

	public Location getHome() {
		return home;
	}

	public int getId() {
		return id;
	}

	public int getMaxPower() {
		return getMembers().size() * FactionPlayer.maxPower;
	}

	public Map<UUID, OlympaFactionRole> getMembers() {
		return members;
	}

	public String getName() {
		return name;
	}

	public String getNamePrefixed(Player player) {
		return getRole(player).getTag() + name;
	}

	public Set<OfflinePlayer> getOfflinePlayer() {
		return members.keySet().stream().filter(uuid -> !getOnlinePlayers().stream().anyMatch(p -> p.getUniqueId().equals(uuid))).map(uuid -> Bukkit.getOfflinePlayer(uuid)).collect(Collectors.toSet());
	}

	public List<String> getOldName() {
		return oldName;
	}

	public Set<Player> getOnlinePlayers() {
		return connected;
	}

	public Set<Player> getOnlinePlayers(OlympaFactionRole minRole) {
		return connected.stream().filter(p -> this.getRole(p).hasPermission(minRole)).collect(Collectors.toSet());
	}

	public int getPower() {
		// TODO add offline players
		return getOnlinePlayers().stream().mapToInt(p -> ((FactionPlayer) AccountProvider.get(p.getUniqueId())).getPower()).sum();
	}

	public OlympaFactionRole getRole(Player player) {
		return getRole(player.getUniqueId());
	}

	public OlympaFactionRole getRole(UUID uuid) {
		return members.get(uuid);
	}

	public String getTag() {
		return tag;
	}

	public boolean isOverClaimable() {
		return getClaims() > getPower();
	}

	// TODO Use id
	@Deprecated
	public boolean isSame(OlympaFaction fChunk) {
		return fChunk.getName().equals(getName());
	}

	public void join(Player player) {
		getOnlinePlayers().forEach(p -> p.sendMessage(Prefix.FACTION + "&2" + player.getName() + "&a a rejoint la faction !"));
		connected.add(player);
		members.put(player.getUniqueId(), OlympaFactionRole.RECRUT);

	}

	public OlympaFactionRole promote(Player player) {
		OlympaFactionRole role = getRole(player).getUpper();
		if (role != null) {
			setRole(player, role);
		}
		return role;
	}

	public void refuse(Player player) {
		getOnlinePlayers().forEach(p -> p.sendMessage(Prefix.FACTION + "&4" + player.getName() + "&c a refusé l'invitation."));
	}

	public void removeConnected(Player player) {
		connected.remove(player);
		getOnlinePlayers().forEach(p -> p.sendMessage(Prefix.FACTION + "&4" + player.getName() + "&c s'est déconnecté."));
	}

	public void setHome(Location home) {
		this.home = home;
	}

	public void setRole(Player player, OlympaFactionRole role) {
		members.put(player.getUniqueId(), role);
	}

	public void unclaim(Chunk chunk) {
		chunks.remove(chunk);
	}
}
