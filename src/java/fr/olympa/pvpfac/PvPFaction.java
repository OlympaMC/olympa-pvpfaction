package fr.olympa.pvpfac;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.pvpfac.factions.commands.FactionCommand;
import fr.olympa.pvpfac.factions.listeners.FactionChatListener;
import fr.olympa.pvpfac.factions.listeners.FactionJoinListener;

public class PvPFaction extends OlympaAPIPlugin {

	private static PvPFaction instance;

	public static PvPFaction getInstance() {
		return instance;
	}

	@Override
	public void onDisable() {

		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactiver.");
	}

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();

		new FactionCommand(this).register();

		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new FactionJoinListener(), this);
		pluginManager.registerEvents(new FactionChatListener(), this);

		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}
}
