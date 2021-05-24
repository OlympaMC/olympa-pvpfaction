package fr.olympa.pvpfac.world;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.editor.RegionEditor;
import fr.olympa.api.region.shapes.Cuboid;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.PvPFactionPermission;
import fr.olympa.pvpfac.world.WorldsManager.WorldType;

public class WorldCommand extends ComplexCommand {

	private final PvPFaction plugin;

	public WorldCommand(final PvPFaction plugin) {
		super(plugin, "world", "Permet de se téléporter d'un monde à un autre.", PvPFactionPermission.WORLD_COMMAND_PLAYER, "worlds");

		this.plugin = plugin;
	}


	@Cmd(player = true, args = "world|nether|end|mining", min = 1, description = "Permet de se téléporter d'un monde à un autre.")
	public void tp(final CommandContext cmd) {
		final WorldType w = WorldType.fromString(cmd.getArgument(0));
		if (w == null) {
			Prefix.FACTION.sendMessage(getPlayer(), "§cCe monde n'existe pas !");
			return;
		}

		w.teleport(getPlayer());
		Prefix.FACTION.sendMessage(getPlayer(), "§aVous allez être téléporté dans le monde " + w.getWorldName() + "...");
	}


	@Cmd(player = true, args = "world|nether|end|mining", min = 1, description = "Permet d'obtenir des informations sur un monde.")
	public void info(final CommandContext cmd) {
		final WorldType w = WorldType.fromString(cmd.getArgument(0));
		if (w == null) {
			Prefix.FACTION.sendMessage(getPlayer(), "§cCe monde n'existe pas !");
			return;
		}

		w.teleport(getPlayer());
		Prefix.FACTION.sendMessage(
			getPlayer(),
			"§cLe monde " + w.getWorldName() + " compte " + w.getWorld().getPlayerCount() + " joueurs et " + w.getWorld().getLoadedChunks().length + " chunks chargés."
		);
	}

	@Cmd(player = true, args = "world|nether|end|mining", min = 1, description = "Permet de définir un portail vers un monde.")
	public void setportal(final CommandContext cmd) {
		if (!PvPFactionPermission.WORLD_COMMAND_ADMIN.hasPermissionWithMsg(getOlympaPlayer())) return;

		final WorldType world = WorldType.fromString(cmd.getArgument(0));

		if (world == null) {
			Prefix.FACTION.sendMessage(getPlayer(), "§cCe monde n'existe pas !");
			return;
		}

		new RegionEditor(getPlayer(), region -> {
			if (!(region instanceof Cuboid)) {
				Prefix.FACTION.sendMessage(getPlayer(), "§cVous devez sélectionner un cuboïde comme nouveau portail.");
				return;
			}

			plugin.getWorldsManager().setPortal(world, (Cuboid) region);
			plugin.getWorldsManager().savePortal(world, (Cuboid) region);
			Prefix.FACTION.sendMessage(getPlayer(), "§aPortail vers le monde " + world.getWorldName() + " mis à jour avec succès.");

		}).enterOrLeave();
	}

	@Cmd(player = true, description = "Permet de supprimer un portail.")
	public void removeportal(final CommandContext cmd) {
		// TODO
	}


}





