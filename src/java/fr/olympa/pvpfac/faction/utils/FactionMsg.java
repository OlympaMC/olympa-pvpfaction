package fr.olympa.pvpfac.faction.utils;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.faction.Faction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FactionMsg {

	public static boolean needArg3ToSelectFaction(final CommandSender sender, final String label, final String[] args) {
		if (args.length < 3) {
			sender.sendMessage(ColorUtils.color(Prefix.DEFAULT_BAD + "Tu dois ajouter un argument tel que &4/" + label + " " + args[1] + " <faction>"));
			return true;
		}
		return false;
	}

	public static boolean youCantWithConsole(final CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ColorUtils.color(Prefix.DEFAULT_BAD + "Impossible avec la console"));
			return true;
		}
		return false;
	}

	//	public static boolean youHaveNoFaction(Player player) {
	//		return youHaveNoFaction(player, ((FactionPlayer) AccountProvider.get(player.getUniqueId())).getFaction());
	//	}

	public static boolean youHaveNoFaction(final Player player, final Faction faction) {
		if (faction == null) {
			player.sendMessage(ColorUtils.color(Prefix.DEFAULT_BAD + "Tu n'as pas de faction."));
			return true;
		}
		return false;
	}
}
