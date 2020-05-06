package fr.olympa.pvpfac;

import java.util.Arrays;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.DynamicLine;
import fr.olympa.api.scoreboard.sign.FixedLine;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.pvpfac.factions.commands.FactionCommand;
import fr.olympa.pvpfac.factions.objects.FactionPlayer;

public class PvPFaction extends OlympaAPIPlugin {

	private static PvPFaction instance;

	public static PvPFaction getInstance() {
		return instance;
	}

	public ScoreboardManager scoreboards;
	public FactionManager factionManager;

	public FactionManager getFactionManager() {
		return factionManager;
	}

	@Override
	public void onDisable() {
		scoreboards.unload();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactiver.");
	}

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();

		OlympaPermission.registerPermissions(PvPFactionPermission.class);
		new FactionCommand(this).register();

		PluginManager pluginManager = getServer().getPluginManager();
		try {
			pluginManager.registerEvents(factionManager = new FactionManager(), this);
		} catch (Exception ex) {
			ex.printStackTrace();
			getLogger().severe("Une erreur est survenue lors de l'initialisation du système de faction.");
		}
		// pluginManager.registerEvents(new FactionJoinListener(), this);
		// .registerEvents(new FactionChatListener(), this);
		// pluginManager.registerEvents(new FactionPvPListener(), this);

		scoreboards = new ScoreboardManager(this, "§6Olympa §e§lZTA", Arrays.asList(
				FixedLine.EMPTY_LINE,
				new DynamicLine<FactionPlayer>(x -> "§eRang : §6" + x.getGroupNameColored()),
				FixedLine.EMPTY_LINE,
				new DynamicLine<FactionPlayer>(x -> "§eMonnaie : §6" + x.getGameMoney().getFormatted(), 1, 0)));

		AccountProvider.setPlayerProvider(FactionPlayer.class, FactionPlayer::new, "pvpfac", FactionPlayer.COLUMNS);
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}

	public void setClansManager(FactionManager clansManager) {
		factionManager = clansManager;
	}
}
