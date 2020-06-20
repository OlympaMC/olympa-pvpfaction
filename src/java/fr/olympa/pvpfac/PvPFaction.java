package fr.olympa.pvpfac;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;

import fr.olympa.api.hook.ProtocolAction;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.api.scoreboard.sign.lines.AnimLine;
import fr.olympa.api.scoreboard.sign.lines.DynamicLine;
import fr.olympa.api.scoreboard.sign.lines.FixedLine;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.pvpfac.faction.FactionChatListener;
import fr.olympa.pvpfac.faction.FactionManager;
import fr.olympa.pvpfac.faction.FactionPvPListener;
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
				new DynamicLine<FactionPlayer>(x -> "§eRang : §6" + x.getGroupNameColored()),
				FixedLine.EMPTY_LINE,
				new DynamicLine<FactionPlayer>(x -> "§eMonnaie : §6" + x.getGameMoney().getFormatted()),
				FixedLine.EMPTY_LINE,
				new AnimLine(this, AnimLine.getAnim("play.olympa.fr", ChatColor.YELLOW, ChatColor.GOLD), 1, 10 * 20));
		
		AccountProvider.setPlayerProvider(FactionPlayer.class, FactionPlayer::new, "pvpfac", FactionPlayer.COLUMNS);
		ProtocolAction protocolSupport = OlympaCore.getInstance().getProtocolSupport();
		if (protocolSupport != null)
			protocolSupport.disable1_8();
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}
	
	public void setClansManager(FactionManager clansManager) {
		factionManager = clansManager;
	}
}
