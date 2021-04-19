package fr.olympa.pvpfac.adminshop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class AdminShopItem {

	TranslatableComponent name;
	float value;
	Material material;

	OlympaItemBuild item;
	long soldToday;
	long totalSold;
	long buyToday;
	long totalBuy;

	boolean actived;

	/**
	 * @param name
	 * @param value
	 * @param material
	 */
	public AdminShopItem(float value, Material material) {
		name = new TranslatableComponent(material.getTranslationKey());
		item = new OlympaItemBuild(material, material.name());
		this.value = value;
		this.material = material;
		updateLore();
	}

	public ItemStack getItemStack() {
		ItemStack itemStack = item.build();
		TranslatableComponent translatable = new TranslatableComponent(material.getTranslationKey());
		itemStack.getItemMeta().setLocalizedName(translatable.toLegacyText());
		return itemStack;
	}

	protected void updateLore() {
		String sep = "&e&m&l##########";
		item.resetLore().lore(sep, "", "&6Valeur &2" + value, "", sep, "", "&7Clique &2Gauche &7> &aAchète", "&7Clique &cDroit &7> &cVends", "", sep);
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

	public OlympaItemBuild getItem() {
		return item;
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

	public boolean isActived() {
		return actived;
	}

	public void setValue(float value) {
		this.value = value;
		updateLore();
	}

	public boolean hasItemOnInv(Player player) {
		return player.getInventory().contains(material);
	}

	public void sold(Player p) {
		int amont = 1;
		p.getInventory().remove(new ItemStack(material, amont));
		soldToday += amont;
		Prefix.FACTION.sendMessage(p, "&cTu as vendu x%d &4%s&c.", amont, material.name());
	}

	public void buy(Player p) {
		int amont = 1;
		p.getInventory().addItem(new ItemStack(material, amont));
		buyToday += amont;
		Prefix.FACTION.sendMessage(p, "&aTu as acheté x%d &2%s&a.", amont, material.name());

	}
}
