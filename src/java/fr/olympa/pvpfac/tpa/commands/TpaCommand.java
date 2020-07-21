package fr.olympa.pvpfac.tpa.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.pvpfac.tpa.TpaHandler;

public class TpaCommand extends OlympaCommand {

	public TpaCommand(Plugin plugin) {
		super(plugin, "tpa", "tpto");
		addArgs(true, "joueur");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			sendUnknownPlayer(args[0]);
			return false;
		}
		TpaHandler.sendRequestTo(player, target);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
