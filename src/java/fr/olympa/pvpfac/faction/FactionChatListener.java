package fr.olympa.pvpfac.faction;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.olympa.api.provider.AccountProvider;
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
			onlines.forEach(p -> p.sendMessage(Prefix.FACTION + " &2" + factionPlayer.getName() + "&a: " + event.getMessage()));
			event.setCancelled(true);
			break;
		case ALLY:
			player.sendMessage(Prefix.FACTION + " &cChat Ally &4En développement.");
			event.setCancelled(true);
			break;
		case GENERAL:
		default:
			return;
		}
	}
}
