package fr.olympa.pvpfac.player;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.enderchest.EnderChestPlayerInterface;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.utils.observable.ObservableInt;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.faction.chat.FactionChat;

public class FactionPlayer extends OlympaPlayerObject implements ClanPlayerInterface<Faction, FactionPlayerData>, EnderChestPlayerInterface {

	public static int POWER_MAX = 5;

	private static final SQLColumn<FactionPlayer> COLUMN_POWER = new SQLColumn<FactionPlayer>("power", "TINYINT(3) NULL DEFAULT 9", Types.TINYINT).setUpdatable();
	private static final SQLColumn<FactionPlayer> COLUMN_ENDER_CHEST = new SQLColumn<FactionPlayer>("ender_chest", "VARBINARY(8000) NULL", Types.VARBINARY).setUpdatable();
	private static final SQLColumn<FactionPlayer> COLUMN_MONEY = new SQLColumn<FactionPlayer>("money", "DOUBLE NULL DEFAULT 0", Types.DOUBLE).setUpdatable();
	
	public static final List<SQLColumn<FactionPlayer>> COLUMNS = Arrays.asList(COLUMN_POWER, COLUMN_ENDER_CHEST, COLUMN_MONEY);
	
	public static FactionPlayer get(Player p) {
		return AccountProvider.get(p.getUniqueId());
	}

	private ObservableInt power = new ObservableInt(0);
	private ItemStack[] enderChestContents;
	private OlympaMoney money = new OlympaMoney(0);
	
	private Faction faction;
	FactionChat chat = FactionChat.GENERAL;

	public FactionPlayer(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
	}
	
	@Override
	public void loaded() {
		super.loaded();
		money.observe("datas", () -> COLUMN_MONEY.updateAsync(this, money.get(), null, null));
		power.observe("datas", () -> COLUMN_POWER.updateAsync(this, power.get(), null, null));
	}

	public FactionChat getChat() {
		return chat;
	}

	@Override
	public Faction getClan() {
		return faction;
	}

	@Override
	public OlympaMoney getGameMoney() {
		return money;
	}

	@Override
	public ItemStack[] getEnderChestContents() {
		return enderChestContents;
	}
	
	@Override
	public void setEnderChestContents(ItemStack[] contents) {
		this.enderChestContents = contents;
		try {
			COLUMN_ENDER_CHEST.updateAsync(this, ItemUtils.serializeItemsArray(enderChestContents), null, null);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int getEnderChestRows() {
		return 1;
	}
	
	public int getPower() {
		return power.get();
	}

	public void setPower(int power) {
		this.power.set(power);
	}

	public boolean addPower() {
		if (power.get() >= POWER_MAX)
			return false;
		else {
			power.increment();
			return true;
		}
	}

	public boolean removePower() {
		if (power.get() <= -POWER_MAX)
			return false;
		else {
			power.decrement();
			return true;
		}
	}

	public boolean hasFactionPermission() {
		return true;
	}

	@Override
	public void loadDatas(ResultSet resultSet) throws SQLException {
		try {
			power.set(resultSet.getInt("power"));
			enderChestContents = ItemUtils.deserializeItemsArray(resultSet.getBytes("ender_chest"));
			money.set(resultSet.getDouble("money"));
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public void removeChat(FactionChat chat) {
		this.chat = chat;
	}

	public void setChat(FactionChat chat) {
		this.chat = chat;
	}

	@Override
	public void setClan(Faction faction) {
		this.faction = faction;
	}
}
