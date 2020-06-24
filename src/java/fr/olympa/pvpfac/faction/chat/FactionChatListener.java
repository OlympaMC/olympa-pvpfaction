package fr.olympa.pvpfac.faction.chat;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.player.FactionPlayer;

public class FactionChatListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		FactionPlayer factionPlayer = AccountProvider.get(player.getUniqueId());
		if (factionPlayer == null)
			return;

		FactionChat chat = factionPlayer.getChat();
		switch (chat) {
		case FACTION:
			Set<Player> onlines = factionPlayer.getClan().getPlayers();
			if (onlines.size() == 1)
				player.sendMessage(Prefix.FACTION + ColorUtils.color("&cTu es seul connecté de ta faction."));
			onlines.forEach(p -> p.sendMessage(Prefix.FACTION + ColorUtils.color("&2" + factionPlayer.getName() + "&a: ") + event.getMessage()));
			event.setCancelled(true);
			break;
		case ALLY:
			player.sendMessage(Prefix.FACTION + ColorUtils.color("&cChat Ally &4En développement."));
			event.setCancelled(true);
			break;
		case GENERAL:
		default:
			return;
		}
	}
}
