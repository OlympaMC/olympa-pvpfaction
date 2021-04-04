package fr.olympa.pvpfac.world;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.PvPFactionPermission;
import fr.olympa.pvpfac.world.WorldsManager.WorldType;

public class WorldCommand extends ComplexCommand {

	private PvPFaction plugin;
	
	public WorldCommand(PvPFaction plugin) {
		super(plugin, "world", "Permet de se téléporter d'un monde à un autre", PvPFactionPermission.TP_WORLDS_COMMANDS, "worlds");
		
		this.plugin = plugin;
	}
	
	
	@Cmd(player = true, args = "world|nether|end|mining", min = 1)
	public void tp(CommandContext cmd) {
		WorldType w = WorldType.fromString(cmd.getArgument(0));
		if (w == null) {
			Prefix.FACTION.sendMessage(getPlayer(), "§cCe monde n'existe pas !");
			return;
		}

		w.teleport(getPlayer());
		Prefix.FACTION.sendMessage(getPlayer(), "§aVous allez être téléporté dans le monde " + w.getWorldName() + "...");
	}
	
	
	@Cmd(player = true, args = "world|nether|end|mining", min = 1)
	public void info(CommandContext cmd) {
		WorldType w = WorldType.fromString(cmd.getArgument(0));
		if (w == null) {
			Prefix.FACTION.sendMessage(getPlayer(), "§cCe monde n'existe pas !");
			return;
		}

		w.teleport(getPlayer());
		Prefix.FACTION.sendMessage(getPlayer(), "§cLe monde " + w.getWorldName() + " compte " + w.getWorld().getPlayerCount() + " joueurs et " + w.getWorld().getLoadedChunks().length + " chunks chargés.");
	}

	
}





