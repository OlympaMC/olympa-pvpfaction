package fr.olympa.pvpfac.adminshop.gui;

import fr.olympa.api.gui.OlympaGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class AdminShopGuiBuy extends OlympaGUI {

	public AdminShopGuiBuy() {
		super("&eAdmin Shop > Vente", 6);
	}

	@Override
	public boolean onClick(final Player p, final ItemStack current, final int slot, final ClickType click) {
		return true;
	}

	@Override
	public boolean onClose(final Player p) {
		return super.onClose(p);
	}
}
