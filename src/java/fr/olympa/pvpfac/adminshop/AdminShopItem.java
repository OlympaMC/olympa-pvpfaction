package fr.olympa.pvpfac.adminshop;

import fr.olympa.api.spigot.item.OlympaItemBuild;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.spigot.utils.SpigotUtils;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Map.Entry;

public class AdminShopItem {

	private static final String SEP = "&e&m&l##########";

	TranslatableComponent name;
	double value;
	int amont;
	@NotNull
	Material material;

	ItemStack item;
	OlympaItemBuild itemBuilder;
	long soldToday;
	long totalSold;
	long buyToday;
	long totalBuy;

	boolean enable;

	public AdminShopItem(final Material material, final double value) {
		this(material, 1, value);
	}

	public AdminShopItem(final Material material, final int amont, final double value) {
		this.amont = amont;
		this.material = material;
		name = new TranslatableComponent(material.getTranslationKey());
		itemBuilder = new OlympaItemBuild(material).size(amont);
		enable = false;
		updateLore();
	}

	protected void updateLore() {
		itemBuilder.resetLore().lore(SEP, "", "&6Valeur &2" + value, "&6Nombre &2" + amont, "", SEP, "", "&7Clique &2Gauche &7> &aAchète", "&7Clique &cDroit &7> &cVends", "", SEP);
	}

	public AdminShopItem(final ItemStack itemStack, final double value) {
		item = itemStack;
		material = itemStack.getType();
		amont = itemStack.getAmount();
		name = new TranslatableComponent(material.getTranslationKey());
		itemBuilder = new OlympaItemBuild(itemStack);
		enable = false;
		updateLore();
	}

	public AdminShopItem enable() {
		enable = true;
		return this;
	}

	public AdminShopItem disable() {
		enable = false;
		return this;
	}

	public boolean hasItemOnInv(final Player player) {
		return player.getInventory().contains(material);
	}

	public void sold(final Player p) {
		final ItemStack it = getRepresentItemStack();
		if (SpigotUtils.containsItems(p.getInventory(), it, amont)) {
			SpigotUtils.removeItems(p.getInventory(), it, amont);
			soldToday += amont;
			Prefix.FACTION.sendMessage(p, "&cTu as vendu &4%s&c à %d.", amont, getClearId(), Double.toString(value / 2));
		} else {
			Prefix.FACTION.sendMessage(p, "&cTu n'as pas &4%s&c dans ton inventaire.", getClearId());
		}
	}

	public ItemStack getRepresentItemStack() {
		ItemStack itemStack = getItemStackOriginal();
		if (itemStack != null) return itemStack;
		itemStack = new ItemStack(material, amont);
		return itemStack;
	}

	public String getClearId() {
		return Utils.capitalize(getId());
	}

	public ItemStack getItemStackOriginal() {
		return item;
	}

	public String getId() {
		return material.name().replace("_", "") + (amont != 1 ? "x" + amont : "");
	}

	public void buy(final Player p) {
		final ItemStack it = getRepresentItemStack();
		if (SpigotUtils.hasEnoughPlace(p.getInventory(), it)) {
			Prefix.FACTION.sendMessage(p, "&aTu as acheté &2%s&a au prix de %s.", getClearId(), Double.toString(value));
			SpigotUtils.giveItems(p, it);
			buyToday += amont;
		} else {
			Prefix.FACTION.sendMessage(p, "&cTu n'as pas la place dans ton inventaire pour &4%s&c.", getClearId());
		}

	}

	public boolean consumeItem(final Player player, int count) {
		final Map<Integer, ? extends ItemStack> ammo = item != null ? player.getInventory().all(item) : player.getInventory().all(material);
		int found = 0;
		for (final ItemStack stack : ammo.values()) {
			found += stack.getAmount();
		}
		if (count > found) return false;

		for (final Entry<Integer, ? extends ItemStack> entry : ammo.entrySet()) {
			final ItemStack stack = entry.getValue();

			final int removed = Math.min(count, stack.getAmount());
			count -= removed;

			if (stack.getAmount() == removed) {
				player.getInventory().setItem(entry.getKey(), null);
			} else {
				stack.setAmount(stack.getAmount() - removed);
			}

			if (count <= 0) break;
		}

		player.updateInventory();
		return true;
	}

	public long getBuyToday() {
		return buyToday;
	}

	public OlympaItemBuild getItemBuilder() {
		return itemBuilder;
	}

	public ItemStack getItemStackAdmin() {
		final OlympaItemBuild itemAdmin = itemBuilder.clone();
		if (!isEnable()) {
			itemAdmin.size(-1);
			itemAdmin.addLoreBefore("&4&lOBJET DESACTIVÉ");
		}
		itemAdmin.lore("", "&4&nActions Admin", "&7Shift-Clique &2Gauche &7> &aActive l'objet", "&7Shift-Clique &cDroit &7> &cDésactive l'objet", SEP);

		return itemAdmin.build();
	}

	public boolean isEnable() {
		return enable;
	}

	public @Nullable ItemStack getItemStackPlayer() {
		if (!isEnable()) {
			return null;
		}
		final ItemStack itemStack = itemBuilder.build();
		//TranslatableComponent translatable = new TranslatableComponent(material.getTranslationKey());
		//itemStack.getItemMeta().setLocalizedName(translatable.toLegacyText());
		return itemStack;
	}

	public @NotNull Material getMaterial() {
		return material;
	}

	public TranslatableComponent getName() {
		return name;
	}

	public long getSoldToday() {
		return soldToday;
	}

	public long getTotalBuy() {
		return totalBuy;
	}

	public long getTotalSold() {
		return totalSold;
	}

	public double getValue() {
		return value;
	}

	public void setValue(final float value) {
		this.value = value;
		updateLore();
	}

}
