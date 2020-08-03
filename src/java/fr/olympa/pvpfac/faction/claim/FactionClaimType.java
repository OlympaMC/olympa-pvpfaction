package fr.olympa.pvpfac.faction.claim;

import java.util.Arrays;

import org.bukkit.ChatColor;

public enum FactionClaimType {

	WILDERNESS("Zone Libre", "§aPvP ON, Pertes de Power ON, Mobs ON", ChatColor.DARK_GREEN),
	SAFEZONE("Zone Sécurisée", "§aDégats OFF", ChatColor.GOLD),
	WARNZONE("Zone PvP", "§cPvP ON, Pertes de Power OFF, Mobs OFF.", ChatColor.DARK_RED),
	AP("Avants-Postes", "§eIci les APs !", ChatColor.AQUA),
	SPAWN("Spawn", "", ChatColor.YELLOW);

	private FactionClaimType(String name, String description, ChatColor color) {
		this.name = name;
		this.description = description;
		this.color = color;
	}

	String name;
	String description;
	ChatColor color;

	public static FactionClaimType get(String name) {
		return Arrays.stream(FactionClaimType.values()).filter(f -> f.name().equals(name)).findFirst().orElse(null);
	}

	public static FactionClaimType get(int type) {
		return Arrays.stream(FactionClaimType.values()).filter(f -> f.ordinal() == type).findFirst().orElse(null);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public ChatColor getColor() {
		return color;
	}

	public String getNameColored() {
		return color + name;
	}

	public String getDescriptionColored() {
		return color + description;
	}
}
