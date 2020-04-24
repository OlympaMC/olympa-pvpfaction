package fr.olympa.pvpfac.factions.scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.pvpfac.factions.scoreboard.api.data.GroupData;
import fr.olympa.pvpfac.factions.scoreboard.api.data.INametag;
import fr.olympa.pvpfac.factions.scoreboard.api.data.PlayerData;
import fr.olympa.pvpfac.factions.scoreboard.api.events.NametagEvent;
import fr.olympa.pvpfac.factions.scoreboard.api.events.NametagFirstLoadedEvent;
import fr.olympa.pvpfac.factions.scoreboard.utils.UUIDFetcher;
import fr.olympa.pvpfac.factions.scoreboard.utils.Utils;

public class NametagHandler implements Listener {

	public static boolean DISABLE_PUSH_ALL_TAGS = false;

	public static boolean isDISABLE_PUSH_ALL_TAGS() {
		return DISABLE_PUSH_ALL_TAGS;
	}

	public static void setDISABLE_PUSH_ALL_TAGS(boolean dISABLE_PUSH_ALL_TAGS) {
		DISABLE_PUSH_ALL_TAGS = dISABLE_PUSH_ALL_TAGS;
	}

	// Multiple threads access resources. We need to make sure we avoid concurrency
	// issues.
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private boolean tabListEnabled = true;
	private boolean longNametagsEnabled = true;
	private boolean refreshTagOnWorldChange = false;
	private BukkitTask clearEmptyTeamTask;
	private BukkitTask refreshNametagTask;
	private List<GroupData> groupData = new ArrayList<>();
	private Map<UUID, PlayerData> playerData = new HashMap<>();
	private Plugin plugin;
	private NametagManager nametagManager;

	public NametagHandler(Plugin plugin, NametagManager nametagManager) {
		this.plugin = plugin;
		this.nametagManager = nametagManager;
		Bukkit.getPluginManager().registerEvents(this, plugin);

		// Apply config properties
		applyConfig();
	}

	void addGroup(GroupData data) {
		try {
			readWriteLock.writeLock().lock();
			groupData.add(data);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	private void applyConfig() {
		clearEmptyTeamTask = createTask(-1, clearEmptyTeamTask, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nte teams clear"));
		refreshNametagTask = createTask(-1, refreshNametagTask, () -> {
			nametagManager.reset();
			applyTags();
		});
	}

	public void applyTags() {
		if (!Bukkit.isPrimaryThread()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					applyTags();
				}
			}.runTask(plugin);
			return;
		}

		for (Player online : Utils.getOnline()) {
			if (online != null) {
				applyTagToPlayer(online, false);
			}
		}
	}

	public void applyTagToPlayer(final Player player, final boolean loggedIn) {
		// If on the primary thread, run async
		if (Bukkit.isPrimaryThread()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					applyTagToPlayer(player, loggedIn);
				}
			}.runTaskAsynchronously(plugin);
			return;
		}

		INametag tempNametag = getPlayerData(player);
		if (tempNametag == null) {
			for (GroupData group : getGroupData()) {
				if (player.hasPermission(group.getBukkitPermission())) {
					tempNametag = group;
					break;
				}
			}
		}

		if (tempNametag == null) {
			return;
		}

		final INametag nametag = tempNametag;
		new BukkitRunnable() {
			@Override
			public void run() {
				nametagManager.setNametag(player.getName(), format(player, nametag.getPrefix(), true),
						format(player, nametag.getSuffix(), true), nametag.getSortPriority());
				// If the TabList is disabled...
				if (!tabListEnabled) {
					// apply the default white username to the player.
					player.setPlayerListName(Utils.format("&f" + player.getPlayerListName()));
				} else {
					if (longNametagsEnabled) {
						player.setPlayerListName(format(player, nametag.getPrefix() + player.getName() + nametag.getSuffix(), false));
					} else {
						player.setPlayerListName(null);
					}
				}

				if (loggedIn) {
					Bukkit.getPluginManager().callEvent(new NametagFirstLoadedEvent(player, nametag));
				}
			}
		}.runTask(plugin);
	}

	public void assignData(List<GroupData> groupData, Map<UUID, PlayerData> playerData) {
		try {
			readWriteLock.writeLock().lock();
			this.groupData = groupData;
			this.playerData = playerData;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public void assignGroupData(List<GroupData> groupData) {
		try {
			readWriteLock.writeLock().lock();
			this.groupData = groupData;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	void clear(final CommandSender sender, final String player) {
		Player target = Bukkit.getPlayerExact(player);
		if (target != null) {
			handleClear(target.getUniqueId(), player);
			return;
		}

		UUIDFetcher.lookupUUID(player, plugin, uuid -> {
			if (uuid == null) {
				NametagMessages.UUID_LOOKUP_FAILED.send(sender);
			} else {
				handleClear(uuid, player);
			}
		});
	}

	public void clearMemoryData() {
		try {
			readWriteLock.writeLock().lock();
			groupData.clear();
			playerData.clear();
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	private BukkitTask createTask(int time, BukkitTask existing, Runnable runnable) {
		if (existing != null) {
			existing.cancel();
		}
		if (time < 0) {
			return null;
		}
		return Bukkit.getScheduler().runTaskTimer(plugin, runnable, 0, 20 * time);
	}

	void deleteGroup(GroupData data) {
		try {
			readWriteLock.writeLock().lock();
			groupData.remove(data);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	/**
	 * Replaces placeholders when a player tag is created. Maxim and Clip's plugins
	 * are searched for, and input is replaced. We use direct imports to avoid any
	 * problems! (So don't change that)
	 */
	public String format(Player player, String input, boolean limitChars) {
		if (input == null) {
			return "";
		}
		if (player == null) {
			return input;
		}
		return Utils.format(input, limitChars);
	}

	public BukkitTask getClearEmptyTeamTask() {
		return clearEmptyTeamTask;
	}

	public List<GroupData> getGroupData() {
		try {
			readWriteLock.writeLock().lock();
			return new ArrayList<>(groupData); // Create a copy instead of unmodifiable
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public GroupData getGroupData(String key) {
		for (GroupData groupData : getGroupData()) {
			if (groupData.getGroupName().equalsIgnoreCase(key)) {
				return groupData;
			}
		}

		return null;
	}

	public NametagManager getNametagManager() {
		return nametagManager;
	}

	public Map<UUID, PlayerData> getPlayerData() {
		return playerData;
	}

	// =================================================
	// Below are methods that we have to be careful with
	// as they can be called from different threads
	// =================================================
	public PlayerData getPlayerData(Player player) {
		return player == null ? null : playerData.get(player.getUniqueId());
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public ReadWriteLock getReadWriteLock() {
		return readWriteLock;
	}

	public BukkitTask getRefreshNametagTask() {
		return refreshNametagTask;
	}

	private void handleClear(UUID uuid, String player) {
		removePlayerData(uuid);
		nametagManager.reset(player);
	}

	public boolean isLongNametagsEnabled() {
		return longNametagsEnabled;
	}

	public boolean isRefreshTagOnWorldChange() {
		return refreshTagOnWorldChange;
	}

	public boolean isTabListEnabled() {
		return tabListEnabled;
	}

	/**
	 * Applies tags to a player
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		nametagManager.sendTeams(player);
	}

	/**
	 * Cleans up any nametag data on the server to prevent memory leaks
	 */
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		nametagManager.reset(event.getPlayer().getName());
	}

	/**
	 * Some users may have different permissions per world. If this is enabled,
	 * their tag will be reloaded on TP.
	 */
	@EventHandler
	public void onTeleport(final PlayerChangedWorldEvent event) {
		if (!refreshTagOnWorldChange) {
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				applyTagToPlayer(event.getPlayer(), false);
			}
		}.runTaskLater(plugin, 3);
	}

	public void reload() {
		applyConfig();
		nametagManager.reset();
	}

	public void removePlayerData(UUID uuid) {
		try {
			readWriteLock.writeLock().lock();
			playerData.remove(uuid);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	void save(CommandSender sender, boolean playerTag, String key, int priority) {
		if (playerTag) {
			Player player = Bukkit.getPlayerExact(key);

			PlayerData data = getPlayerData(player);
			if (data == null) {
				return;
			}

			data.setSortPriority(priority);
		} else {
			GroupData groupData = getGroupData(key);

			if (groupData == null) {
				sender.sendMessage(ChatColor.RED + "Group " + key + " does not exist!");
				return;
			}

			groupData.setSortPriority(priority);
		}
	}

	void save(final CommandSender sender, String targetName, NametagEvent.ChangeType changeType, String value) {
		Player player = Bukkit.getPlayerExact(targetName);

		PlayerData data = getPlayerData(player);
		if (data == null) {
			data = new PlayerData(targetName, null, "", "", -1);
			if (player != null) {
				storePlayerData(player.getUniqueId(), data);
			}
		}

		if (changeType == NametagEvent.ChangeType.PREFIX) {
			data.setPrefix(value);
		} else {
			data.setSuffix(value);
		}

		if (player != null) {
			applyTagToPlayer(player, false);
			data.setUuid(player.getUniqueId());
			return;
		}

		final PlayerData finalData = data;
		UUIDFetcher.lookupUUID(targetName, plugin, uuid -> {
			if (uuid == null) {
				NametagMessages.UUID_LOOKUP_FAILED.send(sender);
			} else {
				storePlayerData(uuid, finalData);
				finalData.setUuid(uuid);
			}
		});
	}

	public void setClearEmptyTeamTask(BukkitTask clearEmptyTeamTask) {
		this.clearEmptyTeamTask = clearEmptyTeamTask;
	}

	public void setGroupData(List<GroupData> groupData) {
		this.groupData = groupData;
	}

	public void setLongNametagsEnabled(boolean longNametagsEnabled) {
		this.longNametagsEnabled = longNametagsEnabled;
	}

	public void setNametagManager(NametagManager nametagManager) {
		this.nametagManager = nametagManager;
	}

	public void setPlayerData(Map<UUID, PlayerData> playerData) {
		this.playerData = playerData;
	}

	public void setPlugin(Plugin plugin) {
		this.plugin = plugin;
	}

	public void setReadWriteLock(ReadWriteLock readWriteLock) {
		this.readWriteLock = readWriteLock;
	}

	public void setRefreshNametagTask(BukkitTask refreshNametagTask) {
		this.refreshNametagTask = refreshNametagTask;
	}

	public void setRefreshTagOnWorldChange(boolean refreshTagOnWorldChange) {
		this.refreshTagOnWorldChange = refreshTagOnWorldChange;
	}

	public void setTabListEnabled(boolean tabListEnabled) {
		this.tabListEnabled = tabListEnabled;
	}

	public void storePlayerData(UUID uuid, PlayerData data) {
		try {
			readWriteLock.writeLock().lock();
			playerData.put(uuid, data);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}
}