package fr.olympa.pvpfac.adminshop;

import java.util.List;

import org.bukkit.Material;

public class AdminShopItems {

	protected static AdminShopItem GRASS = new AdminShopItem(1f, Material.GRASS);
	protected static AdminShopItem DIRT = new AdminShopItem(0.5f, Material.DIRT);

	protected static List<AdminShopItem> items = List.of(GRASS, DIRT);

	protected static List<AdminShopItem> getItems() {
		return items;
	}

}
