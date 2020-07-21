package fr.olympa.pvpfac.tpa;

import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TpaHandler {

	private static Cache<UUID, UUID> requests = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

	public static void addRequest(Player player, Player target) {
		requests.put(player.getUniqueId(), target.getUniqueId());
	}

	public static void removeRequest(Player player) {
		requests.invalidate(player.getUniqueId());
	}

	public static void removeAllRequests(Player player) {
		removeRequest(player);
		Set<Entry<UUID, UUID>> toRemoved = requests.asMap().entrySet().stream().filter(entry -> entry.getValue().equals(player.getUniqueId())).collect(Collectors.toSet());
		if (!toRemoved.isEmpty())
			requests.invalidateAll(toRemoved);
	}

	public static void sendRequestTo(Player player, Player target) {
		addRequest(player, target);
		requests.invalidate(player.getUniqueId());
		TextComponent base = new TextComponent(TextComponent.fromLegacyText("§m§l----------- TPA -----------"));
		base.addExtra("\n\n");
		base.addExtra(new TextComponent(TextComponent.fromLegacyText("§2" + player.getName() + "§7 veut se téléporter à toi.")));
		base.addExtra("\n");
		TextComponent tp = new TextComponent(TextComponent.fromLegacyText("§2[§aOUI§2]"));
		tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, TextComponent.fromLegacyText("§2Accepte la téléportation §lVERS§2 toi.")));
		tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "tpayes " + player.getName()));
		base.addExtra(tp);
		base.addExtra(" ");
		tp = new TextComponent(TextComponent.fromLegacyText("§4[§cNon§4]"));
		tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, TextComponent.fromLegacyText("§4Refuse la téléportation §lVERS§c toi.")));
		tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "tpano " + player.getName()));
		base.addExtra(tp);
		base.addExtra("\n\n");
		target.spigot().sendMessage(base);
	}

	public static void sendRequestHere(Player player, Player target) {
		addRequest(player, target);
		requests.invalidate(player.getUniqueId());
		TextComponent base = new TextComponent(TextComponent.fromLegacyText("§m§l----------- TPA -----------"));
		base.addExtra("\n\n");
		base.addExtra(new TextComponent(TextComponent.fromLegacyText("§4" + player.getName() + "§7 veut que §lTU§7 te téléporte à §lLUI§7.")));
		base.addExtra("\n");
		TextComponent tp = new TextComponent(TextComponent.fromLegacyText("§2[§aOUI§2]"));
		tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, TextComponent.fromLegacyText("§2Accepte de te téléporter à " + player.getName() + ".")));
		tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "tpahereyes " + player.getName()));
		base.addExtra(tp);
		base.addExtra(" ");
		tp = new TextComponent(TextComponent.fromLegacyText("§4[§cNon§4]"));
		tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, TextComponent.fromLegacyText("§4Refuse de te téléporter à " + player.getName() + ".")));
		tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "tpahereno " + player.getName()));
		base.addExtra(tp);
		base.addExtra("\n\n");
		target.spigot().sendMessage(base);
	}
}
