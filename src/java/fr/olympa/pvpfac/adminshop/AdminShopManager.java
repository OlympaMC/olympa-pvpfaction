package fr.olympa.pvpfac.adminshop;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.module.OlympaModule.ModuleApi;
import fr.olympa.api.module.SpigotModule;
import fr.olympa.api.plugin.OlympaAPIPlugin;

public class AdminShopManager implements ModuleApi<OlympaAPIPlugin> {

	protected static AdminShopItem GRASS = new AdminShopItem(1f, Material.GRASS);
	protected static AdminShopItem DIRT = new AdminShopItem(0.5f, Material.DIRT);

	protected List<AdminShopItem> items = List.of(GRASS, DIRT);

	protected List<AdminShopItem> getItems() {
		return items;
	}

	protected AdminShopItem getAdminShopItem(ItemStack item) {
		return items.stream().filter(it -> it.getMaterial().equals(item.getType())).findFirst().orElse(null);
	}

	protected boolean addItem(AdminShopItem item) {
		if (getAdminShopItem(item.getItemStack()) != null)
			return false;
		return items.add(item);
	}

	@Override
	public boolean disable(OlympaAPIPlugin plugin) {
		return false;
	}

	@Override
	public boolean enable(OlympaAPIPlugin plugin) {
		return false;
	}

	@Override
	public boolean setToPlugin(OlympaAPIPlugin plugin) {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	public AdminShopManager(OlympaAPIPlugin pl) {
		OlympaModule<AdminShopManager, Listener, OlympaAPIPlugin, OlympaCommand> adminShopModule = new SpigotModule<>(pl, "adminshop_" + pl.getName(), plugin -> this)
				.cmd(AdminShopCommand.class);
		try {
			adminShopModule.enableModule();
		} catch (Exception e) {
			e.printStackTrace();
		}
		adminShopModule.registerModule();
	}
}
