package fr.olympa.pvpfac.adminshop.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;

public class AdminShopGuiBuy extends OlympaGUI {

	public AdminShopGuiBuy() {
		super("&eAdmin Shop > Vente", 6);
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		return true;
	}

	@Override
	public boolean onClose(Player p) {
		return super.onClose(p);
	}
}