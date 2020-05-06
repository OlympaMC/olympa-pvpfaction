package fr.olympa.pvpfac.factions.objects;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;

import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.objects.OlympaMoney;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.pvpfac.Faction;
import fr.olympa.pvpfac.PvPFaction;

public class FactionPlayer extends OlympaPlayerObject implements ClanPlayerInterface<Faction> {

	public static final Map<String, String> COLUMNS = ImmutableMap.<String, String>builder()
			.put("power", "TINYINT(3) NULL DEFAULT 9")
			.put("ender_chest", "VARBINARY(8000) NULL")
			.put("money", "DOUBLE NULL DEFAULT 0").build();

	static int maxPower = 10;

	public static FactionPlayer get(Player p) {
		return AccountProvider.get(p.getUniqueId());
	}

	int power = 0;
	private ItemStack[] enderChest = new ItemStack[9];
	private OlympaMoney money = new OlympaMoney(0);
	Faction faction;
	FactionChat chat;

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

	public ItemStack[] getEnderChest() {
		return enderChest;
	}

	public OlympaMoney getGameMoney() {
		return money;
	}

	public int getPower() {
		return power;
	}

	public boolean hasFactionPermission() {
		return true;
	}

	@Override
	public void loadDatas(ResultSet resultSet) throws SQLException {
		try {
			enderChest = ItemUtils.deserializeItemsArray(resultSet.getBytes("ender_chest"));
			money.set(resultSet.getDouble("money"));
			faction = PvPFaction.getInstance().getFactionManager().getClan(resultSet.getInt("faction"));
			power = resultSet.getInt("power");
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
			statement.setBytes(i++, ItemUtils.serializeItemsArray(enderChest));
			statement.setDouble(i++, money.get());
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
