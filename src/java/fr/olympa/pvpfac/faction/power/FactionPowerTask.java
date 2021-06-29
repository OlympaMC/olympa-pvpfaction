package fr.olympa.pvpfac.faction.power;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.player.FactionPlayer;

public class FactionPowerTask extends BukkitRunnable {

	FactionPlayer fp;

	public FactionPowerTask(final FactionPlayer fp) {
		this.fp = fp;
	}

	@Override
	public void run() {
		if (!fp.addPower()) {
			//			cancel(); java.lang.IllegalStateException: Not scheduled yet
			PvPFaction.getInstance().getTask().cancelTaskByName("pvpfac_power_" + fp.getUniqueId());
			return;
		}
		Prefix.FACTION.sendMessage((Player) fp.getPlayer(), "&a+1 power (&2%s&a/%s)", fp.getPower(), FactionPlayer.POWER_MAX);
	}

}
