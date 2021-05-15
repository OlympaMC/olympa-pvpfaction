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


	FactionClaimType(final boolean isClaimable, final boolean canPvp, final boolean isProtected, final boolean canPlaceContainers) {
		this.isClaimable = isClaimable;
		this.canPvp = canPvp;
		this.isProtected = isProtected;
		this.canPlaceContainers = canPlaceContainers;
	}

	public static FactionClaimType fromString(final String s) {
		final FactionClaimType obj = null;
		for (final FactionClaimType t : FactionClaimType.values()) {
			if (t.toString().equals(s)) return t;
		}

		return NORMAL;
	}

	public boolean canPvp() {
		return canPvp;
	}

	public boolean canPlaceContainers() {
		return canPlaceContainers;
	}

	public void sendTitle(final Player player) {
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
