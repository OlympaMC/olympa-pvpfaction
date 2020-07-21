package fr.olympa.pvpfac.tpa.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;

public class TpaHereCommand extends OlympaCommand {

	public TpaHereCommand(Plugin plugin) {
		super(plugin, "tpahere", "tphere");
		addArgs(true, "joueur");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
