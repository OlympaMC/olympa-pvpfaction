package fr.olympa.pvpfac.player;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.google.common.collect.ImmutableMap;

import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.faction.chat.FactionChat;

public class FactionPlayer extends OlympaPlayerObject implements ClanPlayerInterface<Faction> {

	public static int POWER_MAX = 10;
	
	public static final Map<String, String> COLUMNS = ImmutableMap.<String, String>builder()
			.put("power", "TINYINT(3) NULL DEFAULT 9")
			.put("ender_chest", "VARBINARY(8000) NULL")
			.put("clan", "INT(11) NULL DEFAULT NULL")
			.put("money", "DOUBLE NULL DEFAULT 0")
			/*.put("inventory", "VARBINARY(8000) NULL")*/.build();

	public static FactionPlayer get(Player p) {
		return AccountProvider.get(p.getUniqueId());
	}

	int power = 0;
	private Inventory enderChest = Bukkit.createInventory(null, 9, "Enderchest de " + getName());
	private OlympaMoney money = new OlympaMoney(0);
	Faction faction;
	FactionChat chat = FactionChat.GENERAL;

	public FactionPlayer(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
	}

	public FactionChat getChat() {
		return chat;
	}

	@Override
	public Faction getClan() {
		return faction;
	}

	public Inventory getEnderChest() {
		return enderChest;
	}

	@Override
	public OlympaMoney getGameMoney() {
		return money;
	}
	
	public int getPower() {
		return power;
	}

	public int setPower(int power) {
		return this.power;
	}
	
	public boolean addPower() {
		if (power + 1 > POWER_MAX)
			return false;
		else {
			power++;
			return true;
		}
	}

	public boolean removePower() {
		if (power - 1 < -POWER_MAX)
			return false;
		else {
			power--;
			return true;
		}
	}

	public boolean hasFactionPermission() {
		return true;
	}

	@Override
	public void loadDatas(ResultSet resultSet) throws SQLException {
		try {
			enderChest.setContents(ItemUtils.deserializeItemsArray(resultSet.getBytes("ender_chest")));
			money.set(resultSet.getDouble("money"));
			faction = PvPFaction.getInstance().getFactionManager().getClan(resultSet.getInt("clan"));
			power = resultSet.getInt("power");
			//			byte[] content = resultSet.getBytes("inventory");
			//			if (content != null && content.length != 0)
			//				getPlayer().getInventory().setStorageContents(ItemUtils.deserializeItemsArray(content));
			//			else
			//				SpigotUtils.clearPlayer(getPlayer());
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public void removeChat(FactionChat chat) {
		this.chat = chat;
	}

	@Override
	public void saveDatas(PreparedStatement statement) throws SQLException {
		try {
			int i = 1;
			statement.setInt(i++, power);
			statement.setBytes(i++, ItemUtils.serializeItemsArray(enderChest.getContents()));
			statement.setInt(i++, faction == null ? -1 : faction.getID());
			statement.setDouble(i++, money.get());
			//			statement.setBytes(i++, ItemUtils.serializeItemsArray(getPlayer().getInventory().getStorageContents()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setChat(FactionChat chat) {
		this.chat = chat;
	}

	@Override
	public void setClan(Faction faction) {
		this.faction = faction;
	}
}
