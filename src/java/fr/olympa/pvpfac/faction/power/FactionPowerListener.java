package fr.olympa.pvpfac.faction.power;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.task.OlympaTask;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.player.FactionPlayer;

public class FactionPowerListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		final FactionPlayer fp = AccountProviderAPI.getter().get(uuid);
		PvPFaction.getInstance().getTask().scheduleSyncRepeatingTask("pvpfac_power_" + uuid, new FactionPowerTask(fp), 30, 60, TimeUnit.MINUTES);
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		PvPFaction.getInstance().getTask().cancelTaskByName("pvpfac_power_" + uuid);
	}

	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		final UUID uuid = player.getUniqueId();
		final FactionPlayer fp = AccountProviderAPI.getter().get(uuid);
		if (!fp.removePower())
			return;

		final OlympaTask task = PvPFaction.getInstance().getTask();
		if (!task.taskExist("pvpfac_power_" + uuid))
			PvPFaction.getInstance().getTask().scheduleSyncRepeatingTask("pvpfac_power_" + uuid, new FactionPowerTask(fp), 30, 60, TimeUnit.MINUTES);
		// TODO Vérifier, il me semble qu'avec le code actuel, on perd 1 de power
		Prefix.FACTION.sendMessage(fp.getPlayer(), "&c-2 powers (&4%s&c/%s)", fp.getPower(), FactionPlayer.POWER_MAX);
	}
}
