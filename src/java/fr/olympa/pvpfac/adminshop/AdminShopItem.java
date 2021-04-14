package fr.olympa.pvpfac.adminshop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.OlympaItemBuild;
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
	}

	public ItemStack getItemStack() {
		ItemStack itemStack = item.build();
		TranslatableComponent translatable = new TranslatableComponent(material.getTranslationKey());
		itemStack.getItemMeta().setLocalizedName(translatable.toLegacyText());
		return itemStack;
	}

	protected void updateLore() {
		String sep = "&e&m&l##########";
		item.resetLore().lore(sep, "", "&6Valeur &2" + value, "", sep);
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
}
