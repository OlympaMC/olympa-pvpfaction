package fr.olympa.pvpfac.faction.claim;

public enum FactionClaimPermLevel {

	NONE(0, "aucune", false, false, false, false, false),
	LVL_1(1, "infliger des dégâts aux entités", true, false, false, false, false),
	LVL_2(2, "niveau précédent, et interagir avec les portes", true, true, false, false, false),
	LVL_3(3, "niveau précédent, et interagir avec les coffres", true, true, true, false, false), //also used for armorstands, itemframes and paintings
	LVL_4(4, "niveau précédent, et construire", true, true, true, true, false),
	LVL_OWNER(
		5,
		"gérer les membres du claim. Attention, une fois défini plus personne n'a accès au claim, sauf si le propriétaire ajoute des joueurs/factions lui-même !",
		true,
		true,
		true,
		true,
		true
	);

	private final int level;
	private final String desc;
	private final boolean canInteractDoors;
	private final boolean canDamageEntities;
	private final boolean canInteractContainers;
	private final boolean canBuild;
	private final boolean canManageMembers;
	private String name;

	FactionClaimPermLevel(final int level, final String desc, final boolean canDamageEntities, final boolean canInteractDoors, final boolean canInteractContainers, final boolean canBuild, final boolean canManageMembers) {
		this.level = level;
		this.desc = desc;
		this.canDamageEntities = canDamageEntities;
		this.canInteractDoors = canInteractDoors;
		this.canInteractContainers = canInteractContainers;
		this.canBuild = canBuild;
		this.canManageMembers = canManageMembers;
	}

	public static FactionClaimPermLevel fromLevel(final int level) {
		for (final FactionClaimPermLevel perm : FactionClaimPermLevel.values()) {
			if (perm.getLevel() == level) {
				return perm;
			}
		}

		return NONE;
	}

	public int getLevel() {
		return level;
	}

	public boolean canDamageEntities() {
		return canDamageEntities;
	}

	public boolean canBuild() {
		return canBuild;
	}

	public boolean canInteractDoors() {
		return canInteractDoors;
	}

	public boolean canInteractContainers() {
		return canInteractContainers;
	}

	public boolean canManageMembers() {
		return canManageMembers;
	}

	public String getDesc() {
		return desc;
	}

	public String getName() {
		return name;
	}


}
