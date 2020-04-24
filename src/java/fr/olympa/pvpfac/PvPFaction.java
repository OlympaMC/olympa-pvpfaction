package fr.olympa.pvpfac;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.pvpfac.factions.commands.FactionCommand;
import fr.olympa.pvpfac.factions.listeners.FactionChatListener;
import fr.olympa.pvpfac.factions.listeners.FactionJoinListener;
import fr.olympa.pvpfac.factions.listeners.FactionPvPListener;
import fr.olympa.pvpfac.factions.objects.FactionPlayer;
import fr.olympa.pvpfac.factions.scoreboard.NametagEdit;

public class PvPFaction extends OlympaAPIPlugin {

	private static PvPFaction instance;

	public static PvPFaction getInstance() {
		return instance;
	}

	private NametagEdit nameTagEdit;

	@Override
	public void onDisable() {
		nameTagEdit.disable();
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
		pluginManager.registerEvents(new FactionPvPListener(), this);

		AccountProvider.setPlayerProvider(FactionPlayer.class, FactionPlayer::new, "pvpfac", FactionPlayer.COLUMNS);
		nameTagEdit = new NametagEdit();
		nameTagEdit.enable(this);
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}
}
