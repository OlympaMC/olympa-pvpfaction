package fr.olympa.pvpfac.faction.claim;

public enum FactionClaimPermLevel {

	NONE(0, "aucune", false, false, false, false, false),
	LVL_1(1, "infliger des dégâts aux entités", true, false, false, false, false),
	LVL_2(2, "niveau précédent, et interagir avec les portes", true, true, false, false, false),
	LVL_3(3, "niveau précédent, et interagir avec les coffres", true, true, true, false, false), //also used for armorstands, itemframes and paintings
	LVL_4(4, "niveau précédent, et construire", true, true, true, true, false),
	LVL_OWNER(5, "gérer les membres du claim. Attention, une fois défini plus personne n'a accès au claim, sauf si le propriétaire ajoute des joueurs/factions lui-même !", true, true, true, true, true),
	;
	
	private int level;
	private String name;
	private String desc;
	private boolean canInterractDoors;
	private boolean canDamageEntities;
	private boolean canInterractContainers;
	private boolean canBuild;
	private boolean canManageMembers;
	
	public static FactionClaimPermLevel fromLevel(int level) {
		for (FactionClaimPermLevel perm : FactionClaimPermLevel.values())
			if (perm.getLevel() == level)
				return perm;
		
		return NONE;
	}
	
	FactionClaimPermLevel(int level, String desc, boolean canDamageEntities, boolean canInterractDoors, boolean canInterractContainers, boolean canBuild, boolean canManageMembers) {
		this.level = level;
		this.desc= desc;
		this.canDamageEntities = canDamageEntities;
		this.canInterractDoors = canInterractDoors;
		this.canInterractContainers = canInterractContainers;
		this.canBuild = canBuild;
		this.canManageMembers = canManageMembers;
	}

	public String getName() {
		return name;
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
