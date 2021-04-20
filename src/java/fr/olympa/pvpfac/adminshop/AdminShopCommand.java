package fr.olympa.pvpfac.adminshop;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.adminshop.gui.AdminShopGui;

public class AdminShopCommand extends ComplexCommand {

	public AdminShopCommand(Plugin plugin) {
		super(plugin, "adminshop", "Boutique pour vendre/acheter des objets.", null, "boutique");
		addArgumentParser("MATERAL", Material.class);
		/*addArgumentParser("MATERAL_TRANSLATED", (sender, string) -> {
			Collection<String> list = Arrays.stream(Material.values()).map(m -> {
				String name = m.getTranslationKey();
				sender.spigot().sendMessage(new TranslatableComponent(name));
				return name;
			}).collect(Collectors.toList());

			return list;
		}, string -> Arrays.stream(Material.values()).map(m -> m.getTranslationKey()).collect(Collectors.toList()),
				x -> String.format("Le type d'object &4%s&c n'existe pas.", x));*/
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		if (isConsole())
			return false;
		new AdminShopGui(player).create(player);
		return true;
	}

	@Cmd(min = 0, description = "Ajoute un item grace à son type", args = { "MATERAL|INTEGER", "FLOAT" })
	public void addItem(CommandContext cmd) {
		Material material;
		int amout = 1;
		float value;
		if (cmd.getArgumentsLength() > 0 && cmd.getArgument(0) instanceof Material)
			material = cmd.getArgument(0);
		else {
			if (cmd.getArgumentsLength() > 0 && cmd.getArgument(0) instanceof Integer)
				amout = cmd.getArgument(0);
			material = player.getInventory().getItemInMainHand().getType();
		}
		value = 1;
		if (cmd.getArgumentsLength() > 1)
			value = cmd.getArgument(1);
		if (((PvPFaction) plugin).getAdminShop().addItem(new AdminShopItem(material, amout, value)))
			sendMessage(Prefix.FACTION, "&a%s&2 a été ajouté à l'AdminShop avec comme valeur &2%d&a.", material.name(), value);
		else
			sendMessage(Prefix.FACTION, "&cImpossible d'ajouter &4%d&c. Il doit surment déjà être dans le shop.", material.name());
	}
}
