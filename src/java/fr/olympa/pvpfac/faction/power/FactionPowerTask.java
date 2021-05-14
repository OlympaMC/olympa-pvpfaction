package fr.olympa.pvpfac.faction.power;

import fr.olympa.api.utils.Prefix;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.player.FactionPlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class FactionPowerTask extends BukkitRunnable {

	FactionPlayer fp;

	public FactionPowerTask(FactionPlayer fp) {
		this.fp = fp;
	}

	@Override
	public void run() {
		if (!fp.addPower()) {
			//			cancel(); java.lang.IllegalStateException: Not scheduled yet
			PvPFaction.getInstance().getTask().cancelTaskByName("pvpfac_power_" + fp.getUniqueId());
			return;
		}
		Prefix.FACTION.sendMessage(fp.getPlayer(), "&a+1 power (&2%s&a/%s)", fp.getPower(), FactionPlayer.POWER_MAX);
	}

}
