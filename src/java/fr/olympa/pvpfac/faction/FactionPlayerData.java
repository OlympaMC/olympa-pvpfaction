package fr.olympa.pvpfac.faction;

import java.sql.SQLException;

import fr.olympa.api.clans.ClanPlayerData;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.utils.observable.ObservableValue;
import fr.olympa.pvpfac.PvPFaction;

public class FactionPlayerData extends ClanPlayerData<Faction, FactionPlayerData> {

	private final ObservableValue<FactionRole> role;
	
	public FactionPlayerData(OlympaPlayerInformations informations) {
		this(informations, FactionRole.RECRUT);
	}
	
	public FactionPlayerData(OlympaPlayerInformations informations, FactionRole role) {
		super(informations);
		this.role = new ObservableValue<>(role);
		this.role.observe("updateSQL", () -> {
			try {
				PvPFaction.getInstance().factionManager.roleColumn.updateValue(this, this.role.get().ordinal());
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public FactionRole getRole() {
		return role.get();
	}
	
	public void setRole(FactionRole role) {
		this.role.set(role);
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
		
	}
	
}
