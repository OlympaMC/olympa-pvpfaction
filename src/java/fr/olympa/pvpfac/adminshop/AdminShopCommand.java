package fr.olympa.pvpfac.adminshop;

import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;

public class AdminShopCommand extends ComplexCommand {

	public AdminShopCommand(Plugin plugin) {
		super(plugin, "adminshop", "Boutique", null);
	}

	@Cmd(otherArg = true)
	public void otherArg(CommandContext cmd) {
		new AdminShopGui().create(player);
	}
}
