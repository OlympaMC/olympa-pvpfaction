package fr.olympa.pvpfac.adminshop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class AdminShopItem {

	private static final String SEP = "&e&m&l##########";

	TranslatableComponent name;
	float value;
	int amont;
	Material material;

	OlympaItemBuild item;
	long soldToday;
	long totalSold;
	long buyToday;
	long totalBuy;

	boolean enable;

	public AdminShopItem(Material material, float value) {
		this(material, 1, value);
	}

	public AdminShopItem(Material material, int amont, float value) {
		this.amont = amont;
		name = new TranslatableComponent(material.getTranslationKey());
		item = new OlympaItemBuild(material).size(amont);
		enable = false;
		updateLore();
	}

	public ItemStack getItemStackPlayer() {
		if (!isEnable())
			return null;
		ItemStack itemStack = item.build();
		//TranslatableComponent translatable = new TranslatableComponent(material.getTranslationKey());
		//itemStack.getItemMeta().setLocalizedName(translatable.toLegacyText());
		return itemStack;
	}

	public ItemStack getItemStackAdmin() {
		OlympaItemBuild itemAdmin = item.clone();
		if (!isEnable()) {
			itemAdmin.size(-1);
			itemAdmin.addLoreBefore("&4&lOBJECT DESACTIVER");
		}
		itemAdmin.lore("", "&4&nActions Admin", "&7Shift-Clique &2Gauche &7> &aActive l'objet", "&7Shift-Clique &cDroit &7> &cDésactive l'objet", SEP);
		ItemStack itemStack = itemAdmin.build();

		return itemStack;
	}

	protected void updateLore() {
		item.resetLore().lore(SEP, "", "&6Valeur &2" + value, "&6Nombre &2" + amont, "", SEP, "", "&7Clique &2Gauche &7> &aAchète", "&7Clique &cDroit &7> &cVends", "", SEP);
	}

	public TranslatableComponent getName() {
		return name;
	}

	public float getValue() {
		return value;
	}

	public Material getMaterial() {
		return material;
	}

	public OlympaItemBuild getItem() {
		return item;
	}

	public long getSoldToday() {
		return soldToday;
	}

	public long getTotalSold() {
		return totalSold;
	}

	public long getBuyToday() {
		return buyToday;
	}

	public long getTotalBuy() {
		return totalBuy;
	}

	public boolean isEnable() {
		return enable;
	}

	public void enable() {
		enable = true;
	}

	public void disable() {
		enable = false;
	}

	public void setValue(float value) {
		this.value = value;
		updateLore();
	}

	public boolean hasItemOnInv(Player player) {
		return player.getInventory().contains(material);
	}

	public void sold(Player p) {
		int amont = 1;
		p.getInventory().remove(new ItemStack(material, amont));
		soldToday += amont;
		Prefix.FACTION.sendMessage(p, "&cTu as vendu x%d &4%s&c.", amont, material.name());
	}

	public void buy(Player p) {
		int amont = 1;
		p.getInventory().addItem(new ItemStack(material, amont));
		buyToday += amont;
		Prefix.FACTION.sendMessage(p, "&aTu as acheté x%d &2%s&a.", amont, material.name());

	}
}
