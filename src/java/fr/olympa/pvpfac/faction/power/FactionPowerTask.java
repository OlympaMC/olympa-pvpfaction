package fr.olympa.pvpfac.faction.power;

import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.player.FactionPlayer;

public class FactionPowerTask extends BukkitRunnable {

	FactionPlayer fp;

	public FactionPowerTask(FactionPlayer fp) {
		this.fp = fp;
	}

	@Override
	public void run() {
		if (!fp.addPower()) {
			cancel();
			return;
		}
		Prefix.FACTION.sendMessage(fp.getPlayer(), "&a+1 power (&2%s&a/%s)", fp.getPower(), FactionPlayer.POWER_MAX);
	}
	
}
