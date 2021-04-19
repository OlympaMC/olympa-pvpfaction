package fr.olympa.pvpfac.adminshop;

import org.bukkit.Material;
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
	}

	@Cmd
	public void open(CommandContext cmd) {
		new AdminShopGui().create(player);
	}

	@Cmd(min = 1, description = "Ajoute un item grace à son type", args = { "MATERAL", "FLOAT" })
	public void addItem(CommandContext cmd) {
		Material material = cmd.getArgument(0);
		float value = 1;
		if (cmd.getArgumentsLength() > 1)
			value = cmd.getArgument(1);
		if (((PvPFaction) plugin).getAdminShop().addItem(new AdminShopItem(value, material)))
			sendMessage(Prefix.FACTION, "&a%s&2 a été ajouté à l'AdminShop avec comme valeur &2%d&a.", material.name(), value);
		else
			sendMessage(Prefix.FACTION, "&cImpossible d'ajouter &4%d&c. Il doit surment déjà être dans le shop.", material.name());
	}
}
