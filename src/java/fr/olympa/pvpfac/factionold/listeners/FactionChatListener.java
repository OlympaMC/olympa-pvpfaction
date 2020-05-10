package fr.olympa.pvpfac.factionold.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.factionold.FactionHandler;
import fr.olympa.pvpfac.factionold.objects.FactionChat;
import fr.olympa.pvpfac.factionold.objects.FactionPlayer;

public class FactionChatListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		FactionPlayer fp = (FactionPlayer) olympaPlayer;
		Faction faction = fp.getClan();
		String message = event.getMessage();

		FactionChat chat = fp.getChat();

		if (faction != null) {
			OlympaGroup group = olympaPlayer.getGroup();
			boolean startWith = message.startsWith("!");
			if (startWith || chat == FactionChat.FACTION) {
				if (startWith) {
					message = message.replaceFirst("^! *", "");
				}
				// event.getRecipients().removeIf(p -> !faction.getOnlinePlayers().contains(p));
				// event.setFormat(Prefix.FACTION + ColorUtils.color("&2") +
				// faction.getRole(player).getName() + " %s " + group.getChatSufix() + " &a%s");
			} else {
				// event.setFormat(group.getPrefix(olympaPlayer.getGender()) +
				// faction.getNamePrefixed(player) + " %s " + group.getChatSufix() + " %s");
			}
		} else if (FactionHandler.isPlayerTryingToCreateFaction(player)) {
			if (message.equalsIgnoreCase("annuler")) {
				FactionHandler.removePlayerTryingToCreateFaction(player);
				player.sendMessage(Prefix.FACTION + ColorUtils.color("&cCréation de faction annulée."));
			}
			String[] args = message.split(" ");

			if (args.length > 1) {
				player.sendMessage(Prefix.FACTION + ColorUtils.color("&aQuel sera le nom de ta faction ? &e(réponds dans le chat, écrit &cAnnuler&e pour annuler)&a."));
				return;
			}
			player.sendMessage(Prefix.FACTION + ColorUtils.color("&6En dev."));
			FactionHandler.removePlayerTryingToCreateFaction(player);

		}
	}
}
