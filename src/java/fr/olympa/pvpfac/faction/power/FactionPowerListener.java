package fr.olympa.pvpfac.faction.power;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.player.FactionPlayer;

public class FactionPowerListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		FactionPlayer fp = AccountProvider.get(uuid);
		PvPFaction.getInstance().getTask().scheduleSyncRepeatingTask("pvpfac_power_" + uuid, new FactionPowerTask(fp), 30 * 60 * 20, 60 * 60 * 20);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		PvPFaction.getInstance().getTask().cancelTaskByName("pvpfac_power_" + uuid);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		UUID uuid = player.getUniqueId();
		FactionPlayer fp = AccountProvider.get(uuid);
		if (!fp.removePower() || !fp.removePower())
			return;
		OlympaTask task = PvPFaction.getInstance().getTask();
		if (!task.taskExist("pvpfac_power_" + uuid))
			PvPFaction.getInstance().getTask().scheduleSyncRepeatingTask("pvpfac_power_" + uuid, new FactionPowerTask(fp), 30 * 60 * 20, 60 * 60 * 20);
		Prefix.FACTION.sendMessage(fp.getPlayer(), "&c-1 power (&4%s&c/%s)", fp.getPower(), FactionPlayer.POWER_MAX);
	}
}
