package fr.olympa.pvpfac.faction;

import fr.olympa.api.clans.ClanPlayerData;
import fr.olympa.api.player.OlympaPlayerInformations;

public class FactionPlayerData extends ClanPlayerData<Faction, FactionPlayerData> {

	public FactionPlayerData(OlympaPlayerInformations informations) {
		super(informations);
	}
	
}
