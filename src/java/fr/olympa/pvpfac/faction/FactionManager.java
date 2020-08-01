package fr.olympa.pvpfac.faction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Chunk;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.clans.gui.ClanManagementGUI;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.Column;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.PvPFactionPermission;
import fr.olympa.pvpfac.faction.FactionPlayerData.FactionRole;
import fr.olympa.pvpfac.faction.claim.FactionClaim;
import fr.olympa.pvpfac.faction.gui.FactionManagementGUI;

public class FactionManager extends ClansManager<Faction, FactionPlayerData> {

	private Cache<FactionClaim, Faction> claimCache;

	protected Column<FactionPlayerData> roleColumn;

	protected Column<Faction> homeColumn;
	protected Column<Faction> typeColumn;
	protected Column<Faction> claimsColumn;
	protected Column<Faction> enemyColumn;
	protected Column<Faction> truceColumn;
	protected Column<Faction> allyColumn;
	protected Column<Faction> descriptionColumn;
	protected Column<Faction> tagColumn;

	public FactionManager() throws SQLException, ReflectiveOperationException {
		super(PvPFaction.getInstance(), "pvpfac_factions", 10);
		new FactionCommand(this, "Permet de gérer les factions.", PvPFactionPermission.FACTION_PLAYERS_COMMAND, "factions", "f", "fac").register();
		claimCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
		//		for (FactionType defaultFac : FactionType.getDefaultFactions().stream().filter(ft -> !getClans().stream().anyMatch(entry -> entry.getValue().getType() == ft)).collect(Collectors.toList())) {
		//
		//			PreparedStatement statement = createDefaultClanStatement.getStatement();
		//			int i = 1;
		//			statement.setString(i++, defaultFac.getDefaultName());
		//			statement.setLong(i++, 2);
		//			statement.setString(i++, defaultFac.getDefaultDesciption());
		//			ResultSet resultSet = statement.getGeneratedKeys();
		//			resultSet.next();
		//			int id = resultSet.getInt("id");
		//			resultSet.close();
		//			super.clans.put(id, new Faction(this, id, defaultFac.getDefaultName(), defaultFac.getDefaultDesciption(), 2, defaultFac));
		//		}
		stringAlreadyInClan = "Ce joueur est déjà dans une faction.";
		stringAlreadyInvited = "Tu as déjà invité ce joueur.";
		stringPlayerInvited = "Tu as invité le joueur à rejoindre ta faction !";
		stringInvitationReceive = "§l%s§r§a t'a invité à rejoindre sa faction : \"§l%s§r§a\" ! §oClique ici ou accepte l'invitation depuis le menu.";
		stringClickToJoin = "§e§lClique pour rejoindre la faction !";
		stringYouAlreadyInClan = "Tu fais déjà partie d'une faction.";
		stringClanAlreadyExists = "Une faction avec ce nom existe déjà.";
		stringClanCreated = "Tu viens de créer ta faction !";
		stringNoInvitation = "Tu n'as pas reçu d'invitation de la part de la faction \"%s\".";
		stringClanJoined = "Tu viens de rejoindre la faction §l\"%s\"§r§a !";
		stringClanFull = "Cette faction n'a plus la place pour accueillir un autre joueur...";
		stringCantLeaveChief = "Tu ne peux pas quitter la faction en en étant le chef. Transfère la direction de celle-ci à un autre joueur.";
		stringCantChiefSelf = "Tu ne peux pas te transférer la direction de ta propre faction.";
		stringPlayerNotInClan = "Le joueur %s ne fait pas partie de la faction.";
		stringMustBeInClan = "Tu dois appartenir à une faction pour faire cette commande.";
		stringMustBeChief = "Tu dois être le chef de la faction pour effectuer cette commande.";
		stringClanDisband = "§lLe clan a été dissous. Ceci est le dernier message que vous recevrez.";
		stringPlayerChief = "Le joueur %s est désormais le chef de la faction.";
		stringPlayerLeave = "Le joueur %s a quitté la faction.";
		stringPlayerJoin = "Le joueur %s rejoint la faction.";
		stringNameChange = "La faction a changé de nom pour s'appeler %s.";
		stringSureDisband = "§7Veux-tu vraiment supprimer la faction ?";
		stringSureChief = "§7Veux-tu vraiment donner la direction au joueur %s ?";
		stringSureKick = "§7Veux-tu vraiment éjecter le joueur %s ?";
		stringSureLeave = "§7Veux-tu vraiment quitter la faction ?";
		stringItemCreate = "§eCréer ma faction";
		stringChooseName = "§aChoisis le nom de ta faction :";
		stringInventoryManage = "Gérer sa faction";
		stringInventoryJoin = "Rejoindre une faction";
		stringItemLeave = "Quitter la faction";
		stringAddedMoney = "Tu viens d'ajouter %s à la cagnotte de la faction !";
		stringItemLeaveChiefLore = new String[] { "§7§oPour pouvoir quitter votre faction,", "§7§ovous devez tout d'abord", "§7§otransmettre la direction de celle-ci", "§7§oà un autre membre." };
		stringClanNameTooLong = "Le nom d'une faction ne peut pas excéder %d caractères !";
		stringItemDisband = "§cDémenteler la faction";
	}

	@Override
	protected String getClansCommand() {
		return "faction";
	}

	@Override
	protected Faction createClan(int id, String name, OlympaPlayerInformations chief, int maxSize) {
		return new Faction(this, id, name, chief, maxSize);
	}

	@Override
	protected Faction provideClan(int id, String name, OlympaPlayerInformations chief, int maxSize, double money, long created, ResultSet resultSet) throws SQLException {
		String jsonClaims = resultSet.getString("claims");
		Set<FactionClaim> claims = new HashSet<>();
		if (jsonClaims != null && !jsonClaims.isBlank())
			claims = new Gson().fromJson(jsonClaims, new TypeToken<Set<FactionClaim>>() {
			}.getType());
		return new Faction(this, id, name, chief, maxSize, money, created, resultSet.getString("tag"), resultSet.getString("description"), SpigotUtils.convertStringToLocation(resultSet.getString("home")), claims,
				FactionType.get(resultSet.getInt("type")));
	}

	@Override
	protected FactionPlayerData createClanData(OlympaPlayerInformations informations) {
		return new FactionPlayerData(informations);
	}

	@Override
	protected FactionPlayerData provideClanData(OlympaPlayerInformations informations, ResultSet resultSet) throws SQLException {
		return new FactionPlayerData(informations, FactionRole.values()[resultSet.getInt("role")]);
	}

	@Override
	public List<Column<Faction>> addDBClansCollums(List<Column<Faction>> columns) {
		columns = super.addDBClansCollums(columns);
		columns.add(tagColumn = new Column<Faction>("tag", "VARCHAR(6) NULL DEFAULT NULL").setUpdatable(true));
		columns.add(descriptionColumn = new Column<Faction>("description", "VARCHAR(100) NULL DEFAULT NULL").setUpdatable(true));
		columns.add(allyColumn = new Column<Faction>("ally", "TEXT(65535) NULL DEFAULT NULL").setUpdatable(true));
		columns.add(truceColumn = new Column<Faction>("truce", "TEXT(65535) NULL DEFAULT NULL").setUpdatable(true));
		columns.add(enemyColumn = new Column<Faction>("enemy", "TEXT(65535) NULL DEFAULT NULL").setUpdatable(true));
		columns.add(claimsColumn = new Column<Faction>("claims", "TEXT(65535) NULL DEFAULT NULL").setUpdatable(true));
		columns.add(typeColumn = new Column<Faction>("type", "TINYINT(1) NOT NULL DEFAULT '0'").setUpdatable(true));
		columns.add(homeColumn = new Column<Faction>("home", "VARCHAR(15) NULL").setUpdatable(true));
		return columns;
	}

	@Override
	public List<Column<FactionPlayerData>> addDBPlayersCollums(List<Column<FactionPlayerData>> columns) {
		columns = super.addDBPlayersCollums(columns);
		columns.add(roleColumn = new Column<FactionPlayerData>("role", "TINYINT NOT NULL DEFAULT " + FactionRole.RECRUT.ordinal()).setUpdatable(true));
		return columns;
	}

	@Override
	public ClanManagementGUI<Faction, FactionPlayerData> provideManagementGUI(ClanPlayerInterface<Faction, FactionPlayerData> player) {
		return new FactionManagementGUI(player, this);
	}

	public void removeCache(Chunk chunk) {
		FactionClaim fc = claimCache.asMap().keySet().stream().filter(e -> e.isChunk(chunk)).findFirst().orElse(null);
		if (fc != null)
			claimCache.invalidate(fc);
	}

	public Faction getByChunk(Chunk chunk) {
		Faction faction = claimCache.asMap().entrySet().stream().filter(e -> e.getKey().isChunk(chunk)).map(e -> e.getValue()).findFirst().orElse(null);
		if (faction == null) {
			faction = getClans().stream().filter(c -> c.getValue().hasClaim(chunk)).map(e -> e.getValue()).findFirst().orElse(null);
			if (faction != null)
				claimCache.put(faction.getClaim(chunk), faction);
		}
		return faction;
	}

	public Faction getByName(String name) {
		return getClans().stream().filter(c -> c.getValue().getName().equalsIgnoreCase(name)).map(c -> c.getValue()).findFirst().orElse(null);
	}

	public Faction getByTag(String tag) {
		return getClans().stream().filter(c -> c.getValue().getTag().equalsIgnoreCase(tag)).map(c -> c.getValue()).findFirst().orElse(null);
	}

	public Faction get(String nameOrTagOrPlayer) throws SQLException {
		Faction faction = getByName(nameOrTagOrPlayer);
		if (faction == null)
			faction = getByTag(nameOrTagOrPlayer);
		if (faction == null) {
			ClanPlayerInterface<Faction, FactionPlayerData> target = AccountProvider.get(nameOrTagOrPlayer);
			faction = target.getClan();
		}
		return faction;
	}

}