package fr.olympa.pvpfac.factions.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.factions.FactionHandler;
import fr.olympa.pvpfac.factions.objects.OlympaFaction;

public class FactionCommand extends OlympaCommand {

	public FactionCommand(Plugin plugin) {
		super(plugin, "faction", "f", "factions");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (player != null) {
			OlympaFaction faction = FactionHandler.getFaction(player);
			if (args.length > 0) {
				switch (args[0]) {
				case "create":
					if (faction != null) {
						sendError("Tu as déjà une faction. Quitte-la pour pouvoir en crée une.");
						return false;
					}
					if (args.length < 3) {
						sendUsage(label);
						return false;
					}
					faction = new OlympaFaction(args[1], args[2], player);
					FactionHandler.addFaction(faction);
					FactionHandler.addPlayer(player, faction);
					sendMessage(Prefix.DEFAULT_GOOD, "Ta faction &2" + faction.getName() + "&a a été crée.");
					break;
				case "invite":
					if (faction != null) {
						sendError("Tu n'as pas de faction.");
						return false;
					}
					break;
				case "join":
					break;
				case "show":
					break;
				case "claim":
					if (faction != null) {
						sendError("Tu n'as pas de faction.");
						return false;
					}
					break;
				case "autoclaim":
					if (faction != null) {
						sendError("Tu n'as pas de faction.");
						return false;
					}
					break;
				case "map":
					break;
				case "automap":
					break;
				case "kick":
					if (faction != null) {
						sendError("Tu n'as pas de faction.");
						return false;
					}
					break;
				case "promote":
					if (faction != null) {
						sendError("Tu n'as pas de faction.");
						return false;
					}
					break;
				case "demote":
					if (faction != null) {
						sendError("Tu n'as pas de faction.");
						return false;
					}
					break;
				case "chat":
					if (faction != null) {
						sendError("Tu n'as pas de faction.");
						return false;
					}
					break;
				}
				return false;
			}
			// send help
		} else {
			// console
		}

		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
