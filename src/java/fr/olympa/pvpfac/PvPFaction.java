package fr.olympa.pvpfac;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.hook.ProtocolAction;
import fr.olympa.api.lines.AnimLine;
import fr.olympa.api.lines.DynamicLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.pvpfac.faction.FactionManager;
import fr.olympa.pvpfac.faction.chat.FactionChatListener;
import fr.olympa.pvpfac.faction.claim.FactionPvPListener;
import fr.olympa.pvpfac.player.FactionPlayer;

public class PvPFaction extends OlympaAPIPlugin {

	private static PvPFaction instance;

	public static PvPFaction getInstance() {
		return instance;
	}

	public ScoreboardManager<FactionPlayer> scoreboards;
	public FactionManager factionManager;

	public FactionManager getFactionManager() {
		return factionManager;
	}
	
	public DynamicLine<Scoreboard<FactionPlayer>> lineMoney = new DynamicLine<>(x -> "§7Monnaie: §6" + x.getOlympaPlayer().getGameMoney().getFormatted());
	public DynamicLine<Scoreboard<FactionPlayer>> lineGroup = new DynamicLine<>(x -> "§7Rang: §b" + x.getOlympaPlayer().getGroupNameColored());

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
		AccountProvider.setPlayerProvider(FactionPlayer.class, FactionPlayer::new, "pvpfac", FactionPlayer.COLUMNS);
		//new FactionCommand(this).register();

		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new FactionChatListener(), this);
		pluginManager.registerEvents(new FactionPvPListener(), this);
		try {
			pluginManager.registerEvents(factionManager = new FactionManager(), this);
		} catch (Exception ex) {
			ex.printStackTrace();
			getLogger().severe("Une erreur est survenue lors de l'initialisation du système de faction.");
		}
		// pluginManager.registerEvents(new FactionJoinListener(), this);
		// .registerEvents(new FactionChatListener(), this);
		// pluginManager.registerEvents(new FactionPvPListener(), this);

		scoreboards = new ScoreboardManager(this, "§6Olympa §e§lPvPFaction").addLines(
				FixedLine.EMPTY_LINE,
				lineMoney,
				FixedLine.EMPTY_LINE,
				lineGroup,
				FixedLine.EMPTY_LINE,
				AnimLine.olympaAnimation());

		ProtocolAction protocolSupport = OlympaCore.getInstance().getProtocolSupport();
		if (protocolSupport != null)
			protocolSupport.disable1_8();
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}
}
