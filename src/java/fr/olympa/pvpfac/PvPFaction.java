package fr.olympa.pvpfac;

import fr.olympa.api.auctions.AuctionsManager;
import fr.olympa.api.command.essentials.BackCommand;
import fr.olympa.api.command.essentials.FeedCommand;
import fr.olympa.api.command.essentials.HealCommand;
import fr.olympa.api.command.essentials.tp.TpaHandler;
import fr.olympa.api.economy.MoneyCommand;
import fr.olympa.api.economy.tax.TaxManager;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.lines.CyclingLine;
import fr.olympa.api.lines.DynamicLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.api.trades.TradesManager;
import fr.olympa.pvpfac.adminshop.AdminShopManager;
import fr.olympa.pvpfac.faction.FactionManager;
import fr.olympa.pvpfac.faction.chat.FactionChatListener;
import fr.olympa.pvpfac.faction.claim.FactionClaimListener;
import fr.olympa.pvpfac.faction.claim.FactionClaimsManager;
import fr.olympa.pvpfac.faction.map.AutoMapListener;
import fr.olympa.pvpfac.faction.power.FactionPowerListener;
import fr.olympa.pvpfac.player.FactionPlayer;
import fr.olympa.pvpfac.world.WorldsManager;
import org.bukkit.plugin.PluginManager;

public class PvPFaction extends OlympaAPIPlugin {

	private static PvPFaction instance;
	private WorldsManager worldsManager;
	private TaxManager taxManager;
	private TradesManager<FactionPlayer> trades;
	private AdminShopManager adminShop;
	public ScoreboardManager<FactionPlayer> scoreboards;
	public FactionManager factionManager;
	public FactionClaimsManager claimsManager;
	public DynamicLine<Scoreboard<FactionPlayer>> lineMoney = new DynamicLine<>(x -> "§7Monnaie: §6" + x.getOlympaPlayer().getGameMoney().getFormatted());
	public DynamicLine<Scoreboard<FactionPlayer>> lineGroup = new DynamicLine<>(x -> "§7Rang: §b" + x.getOlympaPlayer().getGroupNameColored());

	@SuppressWarnings("unchecked")
	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
		OlympaAPIPermissions.TP_COMMAND.setMinGroup(OlympaGroup.DEVP);
		OlympaAPIPermissions.GAMEMODE_COMMAND.setMinGroup(OlympaGroup.DEVP);
		OlympaAPIPermissions.GAMEMODE_COMMAND_CREATIVE.setMinGroup(OlympaGroup.DEVP);
		OlympaAPIPermissions.FLY_COMMAND.setMinGroup(OlympaGroup.DEVP);
		OlympaPermission.registerPermissions(PvPFactionPermission.class);
		AccountProvider.setPlayerProvider(FactionPlayer.class, FactionPlayer::new, "pvpfac", FactionPlayer.COLUMNS);

		try {
			taxManager = new TaxManager(this, PvPFactionPermission.TAX_COMMAND, "pvpfac_tax", 0);
			new AuctionsManager(this, "pvpfac_auctions", taxManager);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		PluginManager pluginManager = getServer().getPluginManager();
		try {
			//pluginManager.registerEvents(new OreListener(), this);
			pluginManager.registerEvents(new FactionChatListener(), this);
			//pluginManager.registerEvents(new FactionPvPListener(), this); remplacé par FactionClaimListener
			//pluginManager.registerEvents(new ArmorStandWithHandListener(), this); intégré à FactionClaimListener
			pluginManager.registerEvents(new FactionClaimListener(), this);
			pluginManager.registerEvents(new FactionPowerListener(), this);
			pluginManager.registerEvents(new AutoMapListener(), this);
			pluginManager.registerEvents(factionManager = new FactionManager(), this);
			pluginManager.registerEvents(claimsManager = new FactionClaimsManager(), this);
			//			pluginManager.registerEvents(new Test(), this);
			pluginManager.registerEvents(new TpaHandler(this, PvPFactionPermission.TPA_COMMANDS), this);

			worldsManager = new WorldsManager(this);
		} catch (Exception ex) {
			ex.printStackTrace();
			getLogger().severe("Une erreur est survenue lors de l'initialisation du système de faction.");
		}

		new MoneyCommand<FactionPlayer>(this, "money", "Gérer son porte-monnaie.", PvPFactionPermission.MONEY_COMMAND, PvPFactionPermission.MONEY_COMMAND_OTHER, PvPFactionPermission.MONEY_COMMAND_MANAGE, "monnaie").register();
		trades = new TradesManager<>(this, 10);

		new HealCommand(this, PvPFactionPermission.MOD_COMMANDS).register();
		new FeedCommand(this, PvPFactionPermission.MOD_COMMANDS).register();
		new BackCommand(this, PvPFactionPermission.MOD_COMMANDS).register();
		adminShop = new AdminShopManager(this);
		adminShop.enable(this);

		//Bukkit.createWorld(WorldCreator.name("minage").generateStructures(false));

		// Not needed now ? OlympaCore.getInstance().getRegionManager().awaitWorldTracking("world", event -> event.getRegion().registerFlags(claimsManager.damageFlag, claimsManager.playerBlocksFlag, claimsManager.playerBlockInteractFlag));
		scoreboards = new ScoreboardManager<FactionPlayer>(this, "§6Olympa §e§lPvPFaction").addLines(
			FixedLine.EMPTY_LINE,
			lineMoney,
			FixedLine.EMPTY_LINE,
			lineGroup).addFooters(
			FixedLine.EMPTY_LINE,
			CyclingLine.olympaAnimation());

		/*IProtocolSupport protocolSupport = OlympaCore.getInstance().getProtocolSupport();
		if (protocolSupport != null)
			protocolSupport.disable1_8();*/
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}

	@Override
	public void onDisable() {
		if (scoreboards != null) {
			scoreboards.unload();
		}
		adminShop.disable(this);
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactivé.");
	}

	public AdminShopManager getAdminShop() {
		return adminShop;
	}

	public FactionClaimsManager getClaimsManager() {
		return claimsManager;
	}

	public FactionManager getFactionManager() {
		return factionManager;
	}

	public static PvPFaction getInstance() {
		return instance;
	}

	public TradesManager<FactionPlayer> getTradesManager() {
		return trades;
	}

	public WorldsManager getWorldsManager() {
		return worldsManager;
	}

}
