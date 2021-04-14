package fr.olympa.pvpfac.adminshop;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.PvPFaction;

public class AdminShopCommand extends ComplexCommand {

	public AdminShopCommand(Plugin plugin) {
		super(plugin, "adminshop", "Boutique", null);
		addArgumentParser("MATERAL", Material.class);
	}

	@Cmd(otherArg = true)
	public void otherArg(CommandContext cmd) {
		new AdminShopGui().create(player);
	}

	@Cmd(min = 1, description = "Ajoute un item grace à son type", args = "MATERAL")
	public void addItem(CommandContext cmd) {
		Material material = cmd.getArgument(0);
		((PvPFaction) plugin).getAdminShop().addItem(new AdminShopItem(0, material));
		sendMessage(Prefix.FACTION, "%d a été ajouté à l'AdminShop.", material.name());
	}
}
