package fr.olympa.pvpfac;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaSpigotPermission;

public class PvPFactionPermission {

	public static final OlympaSpigotPermission ADMINSHOP_ADMIN = new OlympaSpigotPermission(OlympaGroup.ADMIN);
	public static final OlympaSpigotPermission ADMINSHOP_MOD = new OlympaSpigotPermission(OlympaGroup.MODP);

	public static final OlympaSpigotPermission TPA_COMMANDS = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission MONEY_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission MONEY_COMMAND_OTHER = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission MONEY_COMMAND_MANAGE = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);

	public static final OlympaSpigotPermission FACTION_PLAYERS_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission FACTION_MANAGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.MOD);
	
	public static final OlympaSpigotPermission FACTION_BYPASS = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);

	public static final OlympaSpigotPermission TAX_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission MOD_COMMANDS = new OlympaSpigotPermission(OlympaGroup.MOD);

	public static final OlympaSpigotPermission WORLD_COMMAND_PLAYER = new OlympaSpigotPermission(OlympaGroup.PLAYER);

	public static final OlympaSpigotPermission WORLD_COMMAND_ADMIN = new OlympaSpigotPermission(OlympaGroup.PLAYER);
}
