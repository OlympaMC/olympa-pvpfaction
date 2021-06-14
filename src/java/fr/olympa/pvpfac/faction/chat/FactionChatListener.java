package fr.olympa.pvpfac.faction.chat;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.player.FactionPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public class FactionChatListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		final FactionPlayer factionPlayer = AccountProviderAPI.getter().get(player.getUniqueId());
		if (factionPlayer == null) return;

		final FactionChat chat = factionPlayer.getChat();
		switch (chat) {
			case FACTION:
				final Set<Player> onlinePlayers = factionPlayer.getClan().getPlayers();
				if (onlinePlayers.size() == 1) {
					player.sendMessage(Prefix.FACTION + ColorUtils.color("&cTu es seul connecté de ta faction."));
				}
				onlinePlayers.forEach(p -> p.sendMessage(Prefix.FACTION + ColorUtils.color("&2" + factionPlayer.getName() + "&a: ") + event.getMessage()));
				event.setCancelled(true);
				break;
			case ALLY:
				player.sendMessage(Prefix.FACTION + ColorUtils.color("&cChat Ally &4En développement."));
				event.setCancelled(true);
				break;
			case GENERAL:
				if (factionPlayer.getClan() != null) {
					event.setFormat("§7[" + factionPlayer.getClan().getName() + "] §r" + factionPlayer.getGroupNameColored() + " %s §r§7: %s");
				}
			default:
		}
	}
}
