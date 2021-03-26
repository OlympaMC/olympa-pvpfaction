package fr.olympa.pvpfac.faction.claim;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import fr.olympa.pvpfac.faction.Faction;

public enum FactionClaimType {

	WILDERNESS(null, "Zone Libre", "§aPvP ON, Pertes de Power ON, Mobs ON", ChatColor.DARK_GREEN, ClaimPermLevel.LEVEL_4, true, false),
	SAFEZONE(-2, "Zone Sécurisée", "§aDégats OFF", ChatColor.GOLD, ClaimPermLevel.LEVEL_1, false, true),
	WARNZONE(-3, "Zone PvP", "§cPvP ON, Pertes de Power OFF, Mobs OFF.", ChatColor.DARK_RED, ClaimPermLevel.LEVEL_1, true, true),
	AP(-4, "Avants-Postes", "§eIci les APs !", ChatColor.AQUA, ClaimPermLevel.LEVEL_1, true, true),
	SPAWN(-1, "Spawn", "", ChatColor.YELLOW, ClaimPermLevel.LEVEL_1, false, true);

	private FactionClaimType(Integer fakeFactionId, String name, String description, ChatColor color, ClaimPermLevel defaultPermLevel, 
			boolean canPvp, boolean isProtected) {
		this.fakeFactionId = fakeFactionId;
		this.name = name;
		this.description = description;
		this.color = color;
		this.defaultPermLevel = defaultPermLevel;
		this.canPvp = canPvp;
		this.isProtected = isProtected;
	}

	private Integer fakeFactionId;
	private String name;
	private String description;
	private ChatColor color;
	private ClaimPermLevel defaultPermLevel;
	boolean canPvp;
	boolean isProtected;

	public static FactionClaimType get(String name) {
		return Arrays.stream(FactionClaimType.values()).filter(f -> f.name().equals(name)).findFirst().orElse(null);
	}

	public static FactionClaimType getFromFakeId(Integer id) {
		return Arrays.stream(FactionClaimType.values()).filter(f -> f.getFakeFactionId() == id).findFirst().orElse(null);
	}

	public Integer getFakeFactionId() {
		return fakeFactionId;
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
	
	public boolean canPvp() {
		return canPvp;
	}
	
	public boolean isProtected() {
		return isProtected;
	}

	public String getNameColored() {
		return color + name;
	}

	public String getDescriptionColored() {
		return color + description;
	}
	
	public void sendTitle(Player p) {
		p.sendTitle(getNameColored(), getDescriptionColored(), 0, 20, 20);
	}

	public ClaimPermLevel getDefaultPermLevel() {
		return defaultPermLevel;
	}
}
