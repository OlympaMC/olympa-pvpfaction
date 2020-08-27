package fr.olympa.pvpfac;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;

public class PvPFactionPermission {

	public static final OlympaPermission TPA_COMMANDS = new OlympaPermission(OlympaGroup.PLAYER);
	public static final OlympaPermission MONEY_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);
	public static final OlympaPermission MONEY_COMMAND_OTHER = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static final OlympaPermission MONEY_COMMAND_MANAGE = new OlympaPermission(OlympaGroup.RESP_TECH);
	
	public static final OlympaPermission FACTION_PLAYERS_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);
	public static final OlympaPermission FACTION_BYPASS = new OlympaPermission(OlympaGroup.RESP_TECH);
	
	public static final OlympaPermission TAX_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission MOD_COMMANDS = new OlympaPermission(OlympaGroup.MOD);
	
}
