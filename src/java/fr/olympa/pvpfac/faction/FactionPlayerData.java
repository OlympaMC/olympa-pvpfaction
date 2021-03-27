package fr.olympa.pvpfac.faction;

import fr.olympa.api.clans.ClanPlayerData;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.utils.observable.ObservableValue;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.claim.ClaimPermLevel;

public class FactionPlayerData extends ClanPlayerData<Faction, FactionPlayerData> {

	private final ObservableValue<FactionRole> role;
	
	public FactionPlayerData(OlympaPlayerInformations informations) {
		this(informations, FactionRole.RECRUT);
	}
	
	public FactionPlayerData(OlympaPlayerInformations informations, FactionRole role) {
		super(informations);
		this.role = new ObservableValue<>(role);
		this.role.observe("updateSQL", () -> PvPFaction.getInstance().getFactionManager().roleColumn.updateAsync(this, this.role.get().ordinal(), null, null));
	}

	public FactionRole getRole() {
		return role.get();
	}
	
	public void setRole(FactionRole role) {
		this.role.set(role);
	}
	
	public enum FactionRole {
		
		//NE PAS RAJOUTER DE VALEUR ICI SI LE SERVEUR EST EN PRODUCTION !!
		//ou retravailler la classe FactionClaim pour prendre en compte les nouveaux rôles dans l'array de ClaimPermLevel concernant les factions membres du claim
		LEADER(3, 10, "Leader", "**", ClaimPermLevel.LEVEL_4),
		OFFICER(2, 5, "Officier", "*", ClaimPermLevel.LEVEL_4),
		MEMBER(1, 2, "Membre", "+", ClaimPermLevel.LEVEL_3),
		RECRUT(0, 0, "Recrue", "-", ClaimPermLevel.LEVEL_1);
		
		public final int weight;
		public final int power;
		public final String name;
		public final String prefix;
		public final ClaimPermLevel claimLevel;
		
		private FactionRole(int weight, int power, String name, String prefix, ClaimPermLevel level) {
			this.weight = weight;
			this.power = power;
			this.name = name;
			this.prefix = prefix;
			this.claimLevel = level;
		}
		
		public FactionRole getAbove() {
			try {
				return values()[ordinal() - 1];
			}catch (ArrayIndexOutOfBoundsException ex) {
				return null;
			}
		}
		
		public FactionRole getBelow() {
			try {
				return values()[ordinal() + 1];
			}catch (ArrayIndexOutOfBoundsException ex) {
				return null;
			}
		}

		public ClaimPermLevel getPlayerClaimLevel() {
			return claimLevel;
		}
		
	}
	
}
