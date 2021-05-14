package fr.olympa.pvpfac.faction.claim;

import org.bukkit.entity.Player;

public enum FactionClaimType {

	NORMAL(true, true, false, true),
	WARZONE(false, true, true, false),
	AP(true, true, false, false),
	SPAWN(false, false, true, false),
	;

	private final boolean isClaimable;
	private final boolean canPvp;

	//used for build, explosion and mob spawning (except CUSTOM)
	private final boolean isProtected;
	private final boolean canPlaceContainers;


	FactionClaimType(boolean isClaimable, boolean canPvp, boolean isProtected, boolean canPlaceContainers) {
		this.isClaimable = isClaimable;
		this.canPvp = canPvp;
		this.isProtected = isProtected;
		this.canPlaceContainers = canPlaceContainers;
	}

	public static FactionClaimType fromString(String s) {
		FactionClaimType obj = null;
		for (FactionClaimType t : FactionClaimType.values()) {
			if (t.toString().equals(s)) {
				return t;
			}
		}

		return NORMAL;
	}

	public boolean canPvp() {
		return canPvp;
	}

	public boolean canPlaceContainers() {
		return canPlaceContainers;
	}

	public void sendTitle(Player player) {
		// TODO Auto-generated method stub

	}

	public String getName() {
		return this.toString().toLowerCase();
	}

	public boolean isClaimable() {
		return isClaimable;
	}

	public boolean isProtected() {
		return isProtected;
	}
}
