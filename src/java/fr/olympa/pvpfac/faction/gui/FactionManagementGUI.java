package fr.olympa.pvpfac.faction.gui;

import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.clans.gui.ClanManagementGUI;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.player.FactionPlayerData;

public class FactionManagementGUI extends ClanManagementGUI<Faction, FactionPlayerData> {

	public FactionManagementGUI(ClanPlayerInterface<Faction, FactionPlayerData> p, Faction fac, ClansManager<Faction, FactionPlayerData> manager) {
		super(p, fac, manager, 3);
	}

	@Override
	protected int getPlayerSlot(int id) {
		return (id < 5 ? 9 : 13) + id;
	}

	@Override
	protected int getPlayerID(int slot) {
		return slot >= 9 && slot < 15 ? slot - 9 : slot - 13;
	}

}
