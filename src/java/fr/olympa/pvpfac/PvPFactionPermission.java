package fr.olympa.pvpfac;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaSpigotPermission;

public class PvPFactionPermission {

	public static final OlympaSpigotPermission TPA_COMMANDS = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission MONEY_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission MONEY_COMMAND_OTHER = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission MONEY_COMMAND_MANAGE = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	
	public static final OlympaSpigotPermission FACTION_PLAYERS_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission FACTION_BYPASS = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	
	public static final OlympaSpigotPermission TAX_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission MOD_COMMANDS = new OlympaSpigotPermission(OlympaGroup.MOD);
	
}
