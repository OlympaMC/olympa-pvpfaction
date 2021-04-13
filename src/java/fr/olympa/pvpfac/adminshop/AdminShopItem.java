package fr.olympa.pvpfac.adminshop;

import org.bukkit.Material;
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

}
