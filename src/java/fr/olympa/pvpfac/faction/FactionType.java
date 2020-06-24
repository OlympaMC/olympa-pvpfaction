package fr.olympa.pvpfac.faction;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum FactionType {
	
	PLAYER,
	STAFF,
	SAFEZONE("§2Zone sécuriser", "§aDégats OFF"),
	WARZONE("§4Zone PvP", "§cPvP ON, Pertes de Power OFF, Mobs OFF."),
	WILDERNESS("§2Zone Libre", "§aPvP ON, Pertes de Power ON, Mobs ON"),
	AP("§6Avants-Postes", "§eIci les aps !");
	
	public String getDefaultName() {
		return defaultName;
	}

	public String getDefaultDesciption() {
		return defaultDesciption;
	}

	private String defaultName;
	private String defaultDesciption;

	private FactionType() {
	}

	private FactionType(String defaultName, String defaultDesciption) {
		this.defaultName = defaultName;
		this.defaultDesciption = defaultDesciption;
	}

	public static FactionType get(int id) {
		return Arrays.stream(FactionType.values()).filter(ft -> ft.ordinal() == id).findFirst().orElse(null);
	}

	public static List<FactionType> getDefaultFactions() {
		return Arrays.stream(FactionType.values()).filter(ft -> ft.defaultName != null).collect(Collectors.toList());
	}
	
}
