package fr.olympa.pvpfac.adminshop.gui;

import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.PvPFactionPermission;
import fr.olympa.pvpfac.adminshop.AdminShopItem;
import fr.olympa.pvpfac.adminshop.AdminShopManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class AdminShopGui extends OlympaGUI {

	int page = 1;

	public AdminShopGui(final Player player) {
		super("&6Admin Shop ", PvPFaction.getInstance().getAdminShop().getGuiRows());
		updateInventory(player);
	}

	public void updateInventory(final Player player) {
		inv.clear();
		final AdminShopManager adminShopHandler = PvPFaction.getInstance().getAdminShop();
		if (PvPFactionPermission.ADMINSHOP_ADMIN.hasSenderPermission(player)) {
			for (final AdminShopItem item : adminShopHandler.getItemPage(1, null)) {
				inv.addItem(item.getItemStackAdmin());
			}
		} else {
			for (final AdminShopItem item : adminShopHandler.getItemPage(1, true)) {
				inv.addItem(Objects.requireNonNull(item.getItemStackPlayer()));
			}
		}

	}

	@Override
	public boolean onClick(final Player player, final ItemStack current, final int slot, final ClickType click) {
		player.sendMessage("slot " + slot);
		final AdminShopItem adminShopItem = PvPFaction.getInstance().getAdminShop().getAdminShopItem(current);
		if (adminShopItem != null) {
			switch (click) {
				case LEFT:
					adminShopItem.buy(player);
					break;
				case RIGHT:
					if (adminShopItem.hasItemOnInv(player)) {
						adminShopItem.sold(player);
					}
					break;
				case SHIFT_LEFT:
					if (PvPFactionPermission.ADMINSHOP_ADMIN.hasSenderPermission(player)) {
						adminShopItem.enable();
					}
					break;
				case SHIFT_RIGHT:
					if (PvPFactionPermission.ADMINSHOP_ADMIN.hasSenderPermission(player)) {
						adminShopItem.disable();
					}
					break;
				case DOUBLE_CLICK:
				case MIDDLE:
				case NUMBER_KEY:
				default:
					break;
			}
		}
		return true;
	}
	
	@Override
	public boolean noDoubleClick() {
		return false;
	}

	@Override
	public boolean onClickCursor(final Player player, final ItemStack current, final ItemStack cursor, final int slot) {
		player.sendMessage("slot " + slot);
		return false;
	}

	@Override
	public boolean onClose(final Player p) {
		return super.onClose(p);
	}
}
