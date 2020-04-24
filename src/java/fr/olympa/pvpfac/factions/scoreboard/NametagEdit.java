package fr.olympa.pvpfac.factions.scoreboard;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import fr.olympa.pvpfac.factions.scoreboard.api.INametagApi;
import fr.olympa.pvpfac.factions.scoreboard.api.NametagAPI;
import fr.olympa.pvpfac.factions.scoreboard.packets.PacketWrapper;

public class NametagEdit {

	private static INametagApi api;

	public static INametagApi getApi() {
		return api;
	}

	private NametagHandler handler;
	private NametagManager manager;

	public void disable() {
		manager.reset();
	}

	public void enable(Plugin plugin) {
		testCompat();

		manager = new NametagManager(plugin);
		handler = new NametagHandler(plugin, manager);
		plugin.getServer().getPluginCommand("ne").setExecutor(new NametagCommand(handler));
		if (api == null) {
			api = new NametagAPI(handler, manager);
		}
	}

	public NametagHandler getHandler() {
		return handler;
	}

	public NametagManager getManager() {
		return manager;
	}

	private void testCompat() {
		PacketWrapper wrapper = new PacketWrapper("TEST", "&f", "", 0, new ArrayList<>());
		wrapper.send();
		if (wrapper.error == null) {
			return;
		}
		Bukkit.getLogger().severe(new StringBuilder()
				.append("\n------------------------------------------------------\n")
				.append("[WARNING] NametagEdit").append(" Failed to load! [WARNING]")
				.append("\n------------------------------------------------------")
				.append("\nThis might be an issue with reflection. REPORT this:\n> ")
				.append(wrapper.error)
				.append("\nThe plugin will now self destruct.\n------------------------------------------------------")
				.toString());
	}

}