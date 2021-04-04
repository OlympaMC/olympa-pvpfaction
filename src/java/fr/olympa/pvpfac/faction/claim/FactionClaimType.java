package fr.olympa.pvpfac.faction.claim;

import org.bukkit.entity.Player;

public enum FactionClaimType {

	NORMAL(true, true, false, true),
	WARZONE(false, true, true, false),
	AP(true, true, false, false),
	SPAWN(false, false, true, false),
	;
	
	private boolean isClaimable;
	private boolean canPvp;
	
	private boolean isProtected; //used for build, explosion and mob spawning (except CUSTOM)
	private boolean canPlaceContainers;
	
	
	private FactionClaimType(boolean isClaimable, boolean canPvp, boolean isProtected, boolean canPlaceContainers) {
		this.isClaimable = isClaimable;
		this.canPvp = canPvp;
		this.isProtected = isProtected;
		this.canPlaceContainers = canPlaceContainers;
	}

	public boolean isClaimable() {
		return isClaimable;
	}

	public boolean canPvp() {
		return canPvp;
	}
	
	
	public boolean isProtected() {
		return isProtected;
	}

	public boolean canPlaceContainers() {
		return canPlaceContainers;
	}
	
	public static FactionClaimType fromString(String s) {
		FactionClaimType obj = null;
		for (FactionClaimType t : FactionClaimType.values())
			if (t.toString().equals(s))
				return t;
		
		return NORMAL;
	}

	
	

	public void sendTitle(Player player) {
		// TODO Auto-generated method stub
		
	}
}
