package fr.olympa.pvpfac.adminshop;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.module.OlympaModule.ModuleApi;
import fr.olympa.api.module.SpigotModule;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.sort.Sorting;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdminShopManager implements ModuleApi<OlympaAPIPlugin> {

	private CustomConfig config;
	private final OlympaAPIPlugin pl;
	protected static final AdminShopItem GRASS = new AdminShopItem(Material.GRASS, 64, 1f);
	protected static final AdminShopItem DIRT = new AdminShopItem(Material.DIRT, 0.5f);
	protected List<AdminShopItem> items;

	public AdminShopManager(OlympaAPIPlugin pl) {
		this.pl = pl;
		try {
			OlympaModule<AdminShopManager, Listener, OlympaAPIPlugin, OlympaCommand> adminShopModule = new SpigotModule<>(pl, "adminshop_" + pl.getName(), plugin -> this)
				.cmd(AdminShopCommand.class);
			adminShopModule.enableModule();
			adminShopModule.registerModule();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeToConfig() {
		for (AdminShopItem item : items) {
			addItemToConfig(item);
		}
		config.save();
	}

	public void addItemToConfig(AdminShopItem item) {
		config.set("items." + item.getId() + ".material", item.getMaterial());
		config.set("items." + item.getId() + ".amont", item.getMaterial());
		config.set("items." + item.getId() + ".value", item.getMaterial());
		config.set("items." + item.getId() + ".enabled", item.isEnable());
		if (item.getItemStackOriginal() != null) {
			config.getConfigurationSection("items").set(item.getId() + ".item", item.getItemStackOriginal());
		}
		config.save();
	}

	public List<AdminShopItem> getItemPage(int page, Boolean enableOrDisableorAll) {
		Stream<AdminShopItem> stream;
		if (enableOrDisableorAll == null) {
			stream = items.stream().sorted(new Sorting<>(it -> it.isEnable() ? 1 : 0));
		} else if (enableOrDisableorAll) {
			stream = items.stream().filter(it -> it.isEnable());
		} else {
			stream = items.stream().filter(it -> !it.isEnable());
		}
		if (page > 1) {
			stream = stream.skip((page - 1) * getPageSize());
		}
		return stream.limit(getPageSize()).collect(Collectors.toList());
	}

	public AdminShopItem getAdminShopItem(ItemStack item) {
		return items.stream().filter(it -> it.getMaterial().equals(item.getType())).findFirst().orElse(null);
	}

	@Override
	public boolean disable(OlympaAPIPlugin plugin) {
		//writeToConfig();
		return false;
	}

	@Override
	public boolean enable(OlympaAPIPlugin plugin) {
		items = new ArrayList<>();
		loadFromConfig();
		addItem(GRASS.enable());
		addItem(DIRT.enable());
		return false;
	}

	public void loadFromConfig() {
		items.clear();
		config = new CustomConfig(pl, "adminshop");
		@Nullable
		ConfigurationSection configSectionItems = config.getConfigurationSection("items");
		if (configSectionItems != null) {
			AdminShopItem item;
			for (String key : configSectionItems.getKeys(true)) {
				item = new AdminShopItem(
					config.getMaterial(configSectionItems.getCurrentPath() + "." + key + ".material"),
					configSectionItems.getInt(key + ".amont"),
					configSectionItems.getDouble(key + ".value")
				);
				if (item.getValue() > 0 && configSectionItems.getBoolean(key + ".enabled")) {
					item.enable();
				}
				item.item = configSectionItems.getItemStack(key + ".item");
				items.add(item);
			}
		}
	}

	public boolean addItem(AdminShopItem item) {
		if (getItemById(item.getId()) != null) {
			return false;
		}
		items.add(item);
		addItemToConfig(item);
		return true;
	}

	public AdminShopItem getItemById(String id) {
		return items.stream().filter(it -> it.getId().equals(id)).findFirst().orElse(null);
	}

	@Override
	public boolean setToPlugin(OlympaAPIPlugin plugin) {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	public List<AdminShopItem> getAllItems() {
		return items.stream().sorted(new Sorting<>(it -> it.isEnable() ? 1 : 0)).collect(Collectors.toList());
	}

	public List<AdminShopItem> getItemsDisabled() {
		return items.stream().filter(it -> !it.isEnable()).collect(Collectors.toList());
	}

	public int getMaxPageAdmin() {
		return items.size() / getPageSize();
	}

	public int getMaxPlayerPlayer() {
		return getItemsEnabled().size() / getPageSize();
	}

	public List<AdminShopItem> getItemsEnabled() {
		return items.stream().filter(it -> it.isEnable()).collect(Collectors.toList());
	}

	public int getPageSize() {
		return getGuiRows() * 9;
	}

	public int getGuiRows() {
		return 6;
	}
}
