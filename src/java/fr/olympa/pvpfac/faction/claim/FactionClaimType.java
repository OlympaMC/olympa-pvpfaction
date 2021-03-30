package fr.olympa.pvpfac.faction.claim;

import org.bukkit.entity.Player;

public enum FactionClaimType {

	NORMAL,
	WARZONE,
	AP,
	SPAWN,
	;
	
	private boolean isClaimable;
	private boolean canPvp;
	
	private boolean isProtected; //used for build, explosion and mob spawning (except CUSTOM)
	private boolean canUseContainers;
	

	
	public boolean isClaimable() {
		return isClaimable;
	}

	public boolean canPvp() {
		return canPvp;
	}
	
	
	public boolean isProtected() {
		return isProtected;
	}

	public boolean canUseContainers() {
		return canUseContainers;
	}

	
	

	public void sendTitle(Player player) {
		// TODO Auto-generated method stub
		
	}
}
