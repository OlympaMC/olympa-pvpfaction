package fr.olympa.pvpfac.adminshop;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class AdminShopItem {

	private static final String SEP = "&e&m&l##########";

	TranslatableComponent name;
	@NotNull
	double value;
	@NotNull
	int amont;
	@NotNull
	Material material;

	ItemStack item;
	OlympaItemBuild itemBuilder;
	long soldToday;
	long totalSold;
	long buyToday;
	long totalBuy;

	boolean enable;

	public AdminShopItem(Material material, double value) {
		this(material, 1, value);
	}

	public AdminShopItem(ItemStack itemStack, double value) {
		item = itemStack;
		material = itemStack.getType();
		amont = itemStack.getAmount();
		name = new TranslatableComponent(material.getTranslationKey());
		itemBuilder = new OlympaItemBuild(itemStack);
		enable = false;
		updateLore();
	}

	public AdminShopItem(Material material, int amont, double value) {
		this.amont = amont;
		this.material = material;
		name = new TranslatableComponent(material.getTranslationKey());
		itemBuilder = new OlympaItemBuild(material).size(amont);
		enable = false;
		updateLore();
	}

	public ItemStack getRepresentItemStack() {
		ItemStack it = getItemStackOriginal();
		if (it != null)
			return it;
		it = new ItemStack(material, amont);
		return it;
	}

	public ItemStack getItemStackOriginal() {
		return item;
	}

	public ItemStack getItemStackPlayer() {
		if (!isEnable())
			return null;
		ItemStack itemStack = itemBuilder.build();
		//TranslatableComponent translatable = new TranslatableComponent(material.getTranslationKey());
		//itemStack.getItemMeta().setLocalizedName(translatable.toLegacyText());
		return itemStack;
	}

	public ItemStack getItemStackAdmin() {
		OlympaItemBuild itemAdmin = itemBuilder.clone();
		if (!isEnable()) {
			itemAdmin.size(-1);
			itemAdmin.addLoreBefore("&4&lOBJET DESACTIVÉ");
		}
		itemAdmin.lore("", "&4&nActions Admin", "&7Shift-Clique &2Gauche &7> &aActive l'objet", "&7Shift-Clique &cDroit &7> &cDésactive l'objet", SEP);
		ItemStack itemStack = itemAdmin.build();

		return itemStack;
	}

	protected void updateLore() {
		itemBuilder.resetLore().lore(SEP, "", "&6Valeur &2" + value, "&6Nombre &2" + amont, "", SEP, "", "&7Clique &2Gauche &7> &aAchète", "&7Clique &cDroit &7> &cVends", "", SEP);
	}

	public TranslatableComponent getName() {
		return name;
	}

	public String getId() {
		return material.name().replace("_", "") + (amont != 1 ? "x" + amont : "");
	}

	public String getClearId() {
		return Utils.capitalize(getId());
	}

	public double getValue() {
		return value;
	}

	public Material getMaterial() {
		return material;
	}

	public OlympaItemBuild getItemBuilder() {
		return itemBuilder;
	}

	public long getSoldToday() {
		return soldToday;
	}

	public long getTotalSold() {
		return totalSold;
	}

	public long getBuyToday() {
		return buyToday;
	}

	public long getTotalBuy() {
		return totalBuy;
	}

	public boolean isEnable() {
		return enable;
	}

	public AdminShopItem enable() {
		enable = true;
		return this;
	}

	public AdminShopItem disable() {
		enable = false;
		return this;
	}

	public void setValue(float value) {
		this.value = value;
		updateLore();
	}

	public boolean hasItemOnInv(Player player) {
		return player.getInventory().contains(material);
	}

	public void sold(Player p) {
		ItemStack it = getRepresentItemStack();
		if (SpigotUtils.containsItems(p.getInventory(), it, amont)) {
			SpigotUtils.removeItems(p.getInventory(), it, amont);
			soldToday += amont;
			Prefix.FACTION.sendMessage(p, "&cTu as vendu &4%s&c à %d.", amont, getClearId(), Double.toString(value / 2));
		} else
			Prefix.FACTION.sendMessage(p, "&cTu n'as pas &4%s&c dans ton inventaire.", getClearId());
	}

	public void buy(Player p) {
		ItemStack it = getRepresentItemStack();
		if (SpigotUtils.hasEnoughPlace(p.getInventory(), it)) {
			Prefix.FACTION.sendMessage(p, "&aTu as acheté &2%s&a au prix de %s.", getClearId(), Double.toString(value));
			SpigotUtils.giveItems(p, it);
			buyToday += amont;
		} else
			Prefix.FACTION.sendMessage(p, "&cTu n'as pas la place dans ton inventaire pour &4%s&c.", getClearId());

	}

	public boolean consumeItem(Player player, int count) {
		Map<Integer, ? extends ItemStack> ammo;
		int found = 0;
		if (item != null)
			ammo = player.getInventory().all(item);
		else
			ammo = player.getInventory().all(material);
		for (ItemStack stack : ammo.values())
			found += stack.getAmount();
		if (count > found)
			return false;

		for (Integer index : ammo.keySet()) {
			ItemStack stack = ammo.get(index);

			int removed = Math.min(count, stack.getAmount());
			count -= removed;

			if (stack.getAmount() == removed)
				player.getInventory().setItem(index, null);
			else
				stack.setAmount(stack.getAmount() - removed);

			if (count <= 0)
				break;
		}

		player.updateInventory();
		return true;
	}

}
