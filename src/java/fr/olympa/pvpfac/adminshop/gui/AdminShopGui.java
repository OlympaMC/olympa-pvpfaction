package fr.olympa.pvpfac.adminshop.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.PvPFactionPermission;
import fr.olympa.pvpfac.adminshop.AdminShopItem;
import fr.olympa.pvpfac.adminshop.AdminShopManager;

public class AdminShopGui extends OlympaGUI {

	int page = 1;

	public AdminShopGui(Player player) {
		super("&6Admin Shop ", PvPFaction.getInstance().getAdminShop().getGuiRows());
		updateInventory(player);
	}

	public void updateInventory(Player player) {
		inv.clear();
		AdminShopManager adminShopHandler = PvPFaction.getInstance().getAdminShop();
		if (PvPFactionPermission.ADMINSHOP_ADMIN.hasSenderPermission(player))
			for (AdminShopItem item : adminShopHandler.getItemPage(1, (Boolean) null))
				inv.addItem(item.getItemStackAdmin());
		else
			for (AdminShopItem item : adminShopHandler.getItemPage(1, true))
				inv.addItem(item.getItemStackPlayer());

	}

	@Override
	public boolean onClickCursor(Player player, ItemStack current, ItemStack cursor, int slot) {
		player.sendMessage("slot " + slot);
		return false;
	}

	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		player.sendMessage("slot " + slot);
		AdminShopItem adminShopItem = PvPFaction.getInstance().getAdminShop().getAdminShopItem(current);
		if (adminShopItem != null)
			switch (click) {
			case LEFT:
				adminShopItem.buy(player);
				break;
			case RIGHT:
				if (adminShopItem.hasItemOnInv(player))
					adminShopItem.sold(player);
				break;
			case SHIFT_LEFT:
				if (PvPFactionPermission.ADMINSHOP_ADMIN.hasSenderPermission(player))
					adminShopItem.enable();
				break;
			case SHIFT_RIGHT:
				if (PvPFactionPermission.ADMINSHOP_ADMIN.hasSenderPermission(player))
					adminShopItem.disable();
				break;
			case DOUBLE_CLICK:
				break;
			case MIDDLE:
				break;
			case NUMBER_KEY:
				break;
			default:
				break;
			}
		return true;
	}

	@Override
	public boolean onClose(Player p) {
		return super.onClose(p);
	}
}
