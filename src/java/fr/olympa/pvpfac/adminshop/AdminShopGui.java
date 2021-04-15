package fr.olympa.pvpfac.adminshop;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.pvpfac.PvPFaction;

public class AdminShopGui extends OlympaGUI {

	public AdminShopGui() {
		super("&eAdmin Shop", 6);
		for (AdminShopItem item : PvPFaction.getInstance().getAdminShop().getItems())
			inv.addItem(item.getItemStack());
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		AdminShopItem adminShopItem = PvPFaction.getInstance().getAdminShop().getAdminShopItem(current);
		if (adminShopItem != null)
			if (click.equals(ClickType.LEFT)) {
				if (adminShopItem.hasItemOnInv(p))
					adminShopItem.sold(p);
			} else if (click.equals(ClickType.RIGHT))
				adminShopItem.buy(p);

		return true;
	}

	@Override
	public boolean onClose(Player p) {
		return super.onClose(p);
	}
}