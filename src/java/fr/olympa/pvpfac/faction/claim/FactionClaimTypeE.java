package fr.olympa.pvpfac.faction.claim;

import java.util.Arrays;

import org.bukkit.ChatColor;

public enum FactionClaimTypeE {

	WILDERNESS("Zone Libre", "§aPvP ON, Pertes de Power ON, Mobs ON", ChatColor.DARK_GREEN),
	SAFEZONE("Zone Sécurisée", "§aDégats OFF", ChatColor.GOLD),
	WARNZONE("Zone PvP", "§cPvP ON, Pertes de Power OFF, Mobs OFF.", ChatColor.DARK_RED),
	AP("Avants-Postes", "§eIci les APs !", ChatColor.AQUA),
	SPAWN("Spawn", "", ChatColor.YELLOW);

	private FactionClaimTypeE(String name, String description, ChatColor color) {
		this.name = name;
		this.description = description;
		this.color = color;
	}

	String name;
	String description;
	ChatColor color;

	public static FactionClaimTypeE get(String name) {
		return Arrays.stream(FactionClaimTypeE.values()).filter(f -> f.name().equals(name)).findFirst().orElse(null);
	}

	public static FactionClaimTypeE get(int type) {
		return Arrays.stream(FactionClaimTypeE.values()).filter(f -> f.ordinal() == type).findFirst().orElse(null);
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
