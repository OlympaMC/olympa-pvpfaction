package fr.olympa.pvpfac.adminshop;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.adminshop.gui.AdminShopGui;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class AdminShopCommand extends ComplexCommand {

	public AdminShopCommand(Plugin plugin) {
		super(plugin, "adminshop", "Boutique pour vendre/acheter des objets.", null, "boutique");
		addArgumentParser("MATERIAL", Material.class);
		/*addArgumentParser("MATERIAL_TRANSLATED", (sender, string) -> {
			Collection<String> list = Arrays.stream(Material.values()).map(m -> {
				String name = m.getTranslationKey();
				sender.spigot().sendMessage(new TranslatableComponent(name));
				return name;
			}).collect(Collectors.toList());
		
			return list;
		}, string -> Arrays.stream(Material.values()).map(m -> m.getTranslationKey()).collect(Collectors.toList()),
				x -> String.format("Le type d'objets &4%s&c n'existe pas.", x));*/
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		if (isConsole()) return false;
		new AdminShopGui(player).create(player);
		return true;
	}

	@Cmd(description = "Ajoute un item grace à son type", args = { "MATERIAL|INTEGER", "FLOAT" })
	public void addItem(CommandContext cmd) {
		Material material;
		int amout = 1;
		float value;
		if (cmd.getArgumentsLength() > 0 && cmd.getArgument(0) instanceof Material) {
			material = cmd.getArgument(0);
		} else {
			if (cmd.getArgumentsLength() > 0 && cmd.getArgument(0) instanceof Integer) {
				amout = cmd.getArgument(0);
			}
			material = player.getInventory().getItemInMainHand().getType();
		}
		value = 1;
		if (cmd.getArgumentsLength() > 1) {
			value = cmd.getArgument(1);
		}
		if (((PvPFaction) plugin).getAdminShop().addItem(new AdminShopItem(material, amout, value))) {
			sendMessage(Prefix.FACTION, "&a%s&2 a été ajouté à l'AdminShop avec comme valeur &2%f&a.", material.name(), value);
		} else {
			sendMessage(Prefix.FACTION, "&cImpossible d'ajouter &4%d&c. Il doit sûrement déjà être dans le shop.", material.name());
		}
	}

	@Cmd(min = 1, description = "Ajoute un item grace à son type", args = { "INTEGER" })
	public void enableItem(CommandContext cmd) {
		AdminShopManager adminShop = ((PvPFaction) plugin).getAdminShop();
		int index = cmd.getArgument(0);
		List<AdminShopItem> allItem = adminShop.getAllItems();
		AdminShopItem item;
		if (allItem.isEmpty() || index < 0 || allItem.size() - 1 < index) {
			sendMessage(Prefix.FACTION, "&cL'index &4%d&c n'existe pas.", index);
			return;
		}
		item = adminShop.getAllItems().get(index);
		if (item.isEnable()) {
			sendMessage(Prefix.FACTION, "&4%s&c est déjà &4activé&c.", item.getMaterial().name());
			return;
		}
		item.enable();
		sendMessage(Prefix.FACTION, "&2%s&a a été &2activé&a.", item.getMaterial().name());
	}

	@Cmd(min = 1, description = "Désactiver un item grace à son type", args = { "INTEGER" })
	public void disableItem(CommandContext cmd) {
		AdminShopManager adminShop = ((PvPFaction) plugin).getAdminShop();
		int index = cmd.getArgument(0);
		List<AdminShopItem> allItem = adminShop.getAllItems();
		AdminShopItem item;
		if (allItem.isEmpty() || index < 0 || allItem.size() - 1 < index) {
			sendMessage(Prefix.FACTION, "&cL'index &4%d&c n'existe pas.", index);
			return;
		}
		item = adminShop.getAllItems().get(index);
		if (!item.isEnable()) {
			sendMessage(Prefix.FACTION, "&4%s&c est déjà &4désactivé&c.", item.getMaterial().name());
			return;
		}
		item.disable();
		sendMessage(Prefix.FACTION, "&4%s&c a été &4désactiver&c.", item.getMaterial().name());
	}
}
