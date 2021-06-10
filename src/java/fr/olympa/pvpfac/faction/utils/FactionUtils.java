package fr.olympa.pvpfac.faction.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class FactionUtils {

	public static TextComponent getFooter() {
		final TextComponent textComponent = new TextComponent();
		final TextComponent textComponent2 = new TextComponent("---------------------------------------");
		textComponent2.setColor(ChatColor.GOLD);
		textComponent2.setBold(true);
		textComponent2.setStrikethrough(true);
		textComponent.addExtra(textComponent2);
		return textComponent;
	}

	public static TextComponent getHeader() {
		final TextComponent textComponent = new TextComponent();
		TextComponent textComponent2 = new TextComponent("----------------");

		textComponent2.setColor(ChatColor.GOLD);
		textComponent2.setBold(true);
		textComponent2.setStrikethrough(true);
		textComponent.addExtra(textComponent2);

		textComponent2 = new TextComponent(" [");
		textComponent2.setColor(ChatColor.GRAY);
		textComponent.addExtra(textComponent2);

		textComponent2 = new TextComponent("Faction");
		textComponent2.setColor(ChatColor.YELLOW);
		textComponent.addExtra(textComponent2);

		textComponent2 = new TextComponent("] ");
		textComponent2.setColor(ChatColor.GRAY);
		textComponent.addExtra(textComponent2);

		textComponent2 = new TextComponent("----------------\n");
		textComponent2.setColor(ChatColor.GOLD);
		textComponent2.setBold(true);
		textComponent2.setStrikethrough(true);
		textComponent.addExtra(textComponent2);
		return textComponent;
	}

}
