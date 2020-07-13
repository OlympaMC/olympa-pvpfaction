package fr.olympa.pvpfac.faction;

import fr.olympa.api.clans.ClanPlayerData;
import fr.olympa.api.player.OlympaPlayerInformations;

public class FactionPlayerData extends ClanPlayerData<Faction, FactionPlayerData> {

	private FactionRole role = FactionRole.RECRUT;
	
	public FactionPlayerData(OlympaPlayerInformations informations) {
		super(informations);
	}

	public FactionRole getRole() {
		return role;
	}
	
	public void setRole(FactionRole role) {
		this.role = role;
	}
	
	public enum FactionRole {
		
		LEADER(10, "Leader", "**"),
		OFFICER(5, "Officier", "*"),
		MEMBER(2, "Membre", "+"),
		RECRUT(0, "Recrue", "-");
		
		public final int power;
		public final String name;
		public final String prefix;
		
		private FactionRole(int power, String name, String prefix) {
			this.power = power;
			this.name = name;
			this.prefix = prefix;
		}
		
	}
	
}
