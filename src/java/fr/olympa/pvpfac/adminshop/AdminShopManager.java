package fr.olympa.pvpfac.adminshop;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.module.OlympaModule.ModuleApi;
import fr.olympa.api.module.SpigotModule;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.sort.Sorting;

public class AdminShopManager implements ModuleApi<OlympaAPIPlugin> {

	protected static final AdminShopItem GRASS = new AdminShopItem(Material.GRASS, 64, 1f);
	protected static final AdminShopItem DIRT = new AdminShopItem(Material.DIRT, 0.5f);

	protected List<AdminShopItem> items;

	public AdminShopManager(OlympaAPIPlugin pl) {
		items = new ArrayList<>();
		items.add(GRASS.enable());
		items.add(DIRT.enable());
		try {
			OlympaModule<AdminShopManager, Listener, OlympaAPIPlugin, OlympaCommand> adminShopModule = new SpigotModule<>(pl, "adminshop_" + pl.getName(), plugin -> this)
					.cmd(AdminShopCommand.class);
			adminShopModule.enableModule();
			adminShopModule.registerModule();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getGuiRows() {
		return 6;
	}

	public int getPageSize() {
		return getGuiRows() * 9;
	}

	public int getMaxPlayerPlayer() {
		return getItemsEnabled().size() / getPageSize();
	}

	public int getMaxPageAdmin() {
		return items.size() / getPageSize();
	}

	public List<AdminShopItem> getItemPage(int page, Boolean enableOrDisableorAll) {
		Stream<AdminShopItem> stream;
		if (enableOrDisableorAll == null)
			stream = items.stream().sorted(new Sorting<>(it -> it.isEnable() ? 1 : 0));
		else if (enableOrDisableorAll)
			stream = items.stream().filter(it -> it.isEnable());
		else
			stream = items.stream().filter(it -> !it.isEnable());
		return stream.skip((page - 1) * getPageSize()).limit(getPageSize()).collect(Collectors.toList());
	}

	public List<AdminShopItem> getAllItems() {
		return items.stream().sorted(new Sorting<>(it -> it.isEnable() ? 1 : 0)).collect(Collectors.toList());
	}

	public List<AdminShopItem> getItemsEnabled() {
		return items.stream().filter(it -> it.isEnable()).collect(Collectors.toList());
	}

	public List<AdminShopItem> getItemsDisabled() {
		return items.stream().filter(it -> !it.isEnable()).collect(Collectors.toList());
	}

	public AdminShopItem getAdminShopItem(ItemStack item) {
		return items.stream().filter(it -> it.getMaterial().equals(item.getType())).findFirst().orElse(null);
	}

	public boolean addItem(AdminShopItem item) {
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
}
