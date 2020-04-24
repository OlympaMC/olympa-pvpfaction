package fr.olympa.pvpfac.factions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.factions.objects.FactionPlayer;
import fr.olympa.pvpfac.factions.objects.OlympaFaction;

public class FactionMsg {

	public static boolean needArg3ToSelectFaction(CommandSender sender, String label, String[] args) {
		if (args.length < 3) {
			sender.sendMessage(ColorUtils.color(Prefix.DEFAULT_BAD + "Tu dois ajouter un argument tel que &4/" + label + " " + args[1] + " <faction>"));
			return true;
		}
		return false;
	}

	public static boolean youCantWithConsole(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ColorUtils.color(Prefix.DEFAULT_BAD + "Impossible avec la console"));
			return true;
		}
		return false;
	}

	public static boolean youHaveNoFaction(Player player) {
		return youHaveNoFaction(player, ((FactionPlayer) AccountProvider.get(player.getUniqueId())).getFaction());
	}

	public static boolean youHaveNoFaction(Player player, OlympaFaction faction) {
		if (faction == null) {
			player.sendMessage(ColorUtils.color(Prefix.DEFAULT_BAD + "Tu n'as pas de faction."));
			return true;
		}
		return false;
	}
}
