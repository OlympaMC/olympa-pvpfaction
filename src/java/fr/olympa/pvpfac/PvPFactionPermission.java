package fr.olympa.pvpfac;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;

public class PvPFactionPermission {

	public static final OlympaPermission TPA_COMMANDS = new OlympaPermission(OlympaGroup.PLAYER);
	
	public static final OlympaPermission FACTION_PLAYERS_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);
	public static final OlympaPermission FACTION_BYPASS = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission FACTION_TAX_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	
}
