package fr.olympa.pvpfac.faction.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import fr.olympa.pvpfac.faction.Faction;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class FactionUtils {
	
	public static TextComponent getFirstSep() {
		TextComponent textComponent = new TextComponent();
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
	
	public static TextComponent getLastSep() {
		TextComponent textComponent = new TextComponent();
		TextComponent textComponent2 = new TextComponent("---------------------------------------");
		textComponent2.setColor(ChatColor.GOLD);
		textComponent2.setBold(true);
		textComponent2.setStrikethrough(true);
		textComponent.addExtra(textComponent2);
		return textComponent;
	}
	
	private static List<String> symboles = Arrays.asList("#", "%", "&", "$", "@", "=", "+", "A", "B", "C", "D", "E", "G", "0", "7");
	
	public static String getChunkLetter(Faction fChunk, Map<Faction, String> factions, int indexSymbole, Faction faction) {
		if (fChunk == null)
			return "-";
		String symb = factions.get(fChunk);
		if (symb == null) {
			symb = symboles.get(indexSymbole++);
			factions.put(fChunk, symb);
		}
		ChatColor color = ChatColor.RED;
		if (faction != null && faction.getID() == fChunk.getID())
			color = ChatColor.GREEN;
		return color + symb;
	}
	
}
