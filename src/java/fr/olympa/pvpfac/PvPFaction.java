package fr.olympa.pvpfac;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.command.essentials.tp.TpaHandler;
import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.lines.CyclingLine;
import fr.olympa.api.lines.DynamicLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.pvpfac.armorstand.ArmorStandWithHandListener;
import fr.olympa.pvpfac.faction.FactionManager;
import fr.olympa.pvpfac.faction.chat.FactionChatListener;
import fr.olympa.pvpfac.faction.claim.FactionClaimEnterListener;
import fr.olympa.pvpfac.faction.claim.FactionClaimProtectionListener;
import fr.olympa.pvpfac.faction.claim.FactionClaimsManager;
import fr.olympa.pvpfac.faction.claim.FactionPvPListener;
import fr.olympa.pvpfac.faction.map.AutoMapListener;
import fr.olympa.pvpfac.faction.power.FactionPowerListener;
import fr.olympa.pvpfac.player.FactionPlayer;

public class PvPFaction extends OlympaAPIPlugin {

	private static PvPFaction instance;

	public static PvPFaction getInstance() {
		return instance;
	}

	public ScoreboardManager<FactionPlayer> scoreboards;
	public FactionManager factionManager;
	public FactionClaimsManager factionClaimsManager;

	public FactionManager getFactionManager() {
		return factionManager;
	}

	public FactionClaimsManager getClaimsManager() {
		return factionClaimsManager;
	}

	public DynamicLine<Scoreboard<FactionPlayer>> lineMoney = new DynamicLine<>(x -> "§7Monnaie: §6" + x.getOlympaPlayer().getGameMoney().getFormatted());
	public DynamicLine<Scoreboard<FactionPlayer>> lineGroup = new DynamicLine<>(x -> "§7Rang: §b" + x.getOlympaPlayer().getGroupNameColored());

	@Override
	public void onDisable() {
		if (scoreboards != null)
			scoreboards.unload();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();

		OlympaPermission.registerPermissions(PvPFactionPermission.class);
		AccountProvider.setPlayerProvider(FactionPlayer.class, FactionPlayer::new, "pvpfac", FactionPlayer.COLUMNS);
		//new FactionCommand(this).register();

		PluginManager pluginManager = getServer().getPluginManager();
		//		pluginManager.registerEvents(new OreListener(), this);
		try {
			pluginManager.registerEvents(new FactionChatListener(), this);
			pluginManager.registerEvents(new FactionPvPListener(), this);
			pluginManager.registerEvents(new FactionClaimEnterListener(), this);
			pluginManager.registerEvents(new FactionPowerListener(), this);
			pluginManager.registerEvents(new AutoMapListener(), this);
			pluginManager.registerEvents(new FactionClaimProtectionListener(), this);
			pluginManager.registerEvents(factionManager = new FactionManager(), this);
			pluginManager.registerEvents(factionClaimsManager = new FactionClaimsManager(), this);
			pluginManager.registerEvents(new TpaHandler(this, PvPFactionPermission.TPA_COMMANDS), this);
		} catch (Exception ex) {
			ex.printStackTrace();
			getLogger().severe("Une erreur est survenue lors de l'initialisation du système de faction.");
		}
		pluginManager.registerEvents(new ArmorStandWithHandListener(), this);

		scoreboards = new ScoreboardManager<FactionPlayer>(this, "§6Olympa §e§lPvPFaction").addLines(
				FixedLine.EMPTY_LINE,
				lineMoney,
				FixedLine.EMPTY_LINE,
				lineGroup).addFooters(
						FixedLine.EMPTY_LINE,
						CyclingLine.olympaAnimation());

		IProtocolSupport protocolSupport = OlympaCore.getInstance().getProtocolSupport();
		if (protocolSupport != null)
			protocolSupport.disable1_8();
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}
}
