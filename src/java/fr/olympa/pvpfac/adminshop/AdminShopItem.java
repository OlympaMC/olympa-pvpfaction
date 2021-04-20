package fr.olympa.pvpfac.adminshop;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class AdminShopItem {

	private static final String SEP = "&e&m&l##########";

	TranslatableComponent name;
	float value;
	int amont;
	Material material;

	ItemStack item;
	OlympaItemBuild itemBuilder;
	long soldToday;
	long totalSold;
	long buyToday;
	long totalBuy;

	boolean enable;

	public AdminShopItem(Material material, float value) {
		this(material, 1, value);
	}

	public AdminShopItem(ItemStack itemStack, float value) {
		item = itemStack;
		material = itemStack.getType();
		amont = itemStack.getAmount();
		name = new TranslatableComponent(material.getTranslationKey());
		itemBuilder = new OlympaItemBuild(itemStack);
		enable = false;
		updateLore();
	}

	public AdminShopItem(Material material, int amont, float value) {
		this.amont = amont;
		name = new TranslatableComponent(material.getTranslationKey());
		itemBuilder = new OlympaItemBuild(material).size(amont);
		enable = false;
		updateLore();
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
			itemAdmin.addLoreBefore("&4&lOBJECT DESACTIVER");
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

	public float getValue() {
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

	public void enable() {
		enable = true;
	}

	public void disable() {
		enable = false;
	}

	public void setValue(float value) {
		this.value = value;
		updateLore();
	}

	public boolean hasItemOnInv(Player player) {
		return player.getInventory().contains(material);
	}

	public void sold(Player p) {
		p.getInventory().remove(new ItemStack(material, amont));
		soldToday += amont;
		Prefix.FACTION.sendMessage(p, "&cTu as vendu x%d &4%s&c.", amont, material.name());
	}

	public void buy(Player p) {
		p.getInventory().addItem(new ItemStack(material, amont));
		buyToday += amont;
		Prefix.FACTION.sendMessage(p, "&aTu as acheté x%d &2%s&a.", amont, material.name());

	}

	public boolean consumeItem(Player player, int count) {
		Map<Integer, ? extends ItemStack> ammo;

		if (item != null)
			ammo = player.getInventory().all(item);
		else
			ammo = player.getInventory().all(material);

		int found = 0;
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
