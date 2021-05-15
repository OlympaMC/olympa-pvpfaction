package fr.olympa.pvpfac.faction.gui;

import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.clans.gui.ClanManagementGUI;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.player.FactionPlayerData;

public class FactionManagementGUI extends ClanManagementGUI<Faction, FactionPlayerData> {

	public FactionManagementGUI(final ClanPlayerInterface<Faction, FactionPlayerData> p, final Faction fac, final ClansManager<Faction, FactionPlayerData> manager) {
		super(p, fac, manager, 3);
	}

	@Override
	protected int getPlayerSlot(final int id) {
		return (id < 5 ? 9 : 13) + id;
	}

	@Override
	protected int getPlayerID(final int slot) {
		return slot >= 9 && slot < 15 ? slot - 9 : slot - 13;
	}

}
