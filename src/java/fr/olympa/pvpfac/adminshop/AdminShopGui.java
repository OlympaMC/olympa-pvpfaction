package fr.olympa.pvpfac.adminshop;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;

public class AdminShopGui extends OlympaGUI {

	public AdminShopGui() {
		super("&eAdmin Shop", 6);
		for (AdminShopItem item : AdminShopItems.getItems())
			inv.addItem(item.getItemStack());
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
