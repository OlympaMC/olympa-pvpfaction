package fr.olympa.pvpfac.faction.claim;

public enum ClaimPermLevel {

	LEVEL_NONE(0, "aucune", false, false, false, false, false),
	LEVEL_1(1, "dégâts aux entités", true, true, false, false, false),
	LEVEL_2(2, "interraction", true, false, false, false, false),
	LEVEL_3(3, "interraction", true, true, true, false, false), //also used for armorstands, itemframes and paintings
	LEVEL_4(4, "construire et ouvrir les coffres", true, true, true, true, false),
	LEVEL_OWNER(5, "propriétaire", true, true, true, true, true),
	;
	
	private int level;
	private String desc;
	private boolean canInterractDoors;
	private boolean canDamageEntities;
	private boolean canInterractContainers;
	private boolean canBuild;
	private boolean canManageMembers;
	
	public static ClaimPermLevel fromLevel(int level) {
		for (ClaimPermLevel perm : ClaimPermLevel.values())
			if (perm.getLevel() == level)
				return perm;
		
		return LEVEL_NONE;
	}
	
	ClaimPermLevel(int level, String desc, boolean canDamageEntities, boolean canInterractDoors, boolean canInterractContainers, boolean canBuild, boolean canManageMembers) {
		this.level = level;
		this.desc= desc;
		this.canDamageEntities = canDamageEntities;
		this.canInterractDoors = canInterractDoors;
		this.canInterractContainers = canInterractContainers;
		this.canBuild = canBuild;
		this.canManageMembers = canManageMembers;
	}


	public int getLevel() {
		return level;
	}

	public String getDesc() {
		return desc;
	}

	public boolean canDamageEntities() {
		return canDamageEntities;
	}
	
	public boolean canBuild() {
		return canBuild;
	}

	public boolean canInterractDoors() {
		return canInterractDoors;
	}

	public boolean canInterractContainers() {
		return canInterractContainers;
	}


	public boolean canManageMembers() {
		return canManageMembers;
	}
	
	
}
