package fr.olympa.pvpfac.factions.scoreboard.api;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.pvpfac.factions.scoreboard.NametagHandler;
import fr.olympa.pvpfac.factions.scoreboard.NametagManager;
import fr.olympa.pvpfac.factions.scoreboard.api.data.FakeTeam;
import fr.olympa.pvpfac.factions.scoreboard.api.data.GroupData;
import fr.olympa.pvpfac.factions.scoreboard.api.data.Nametag;
import fr.olympa.pvpfac.factions.scoreboard.api.events.NametagEvent;

/**
 * Implements the INametagAPI interface. There only exists one instance of this
 * class.
 */
public final class NametagAPI implements INametagApi {

	private NametagHandler handler;

	private NametagManager manager;

	public NametagAPI(NametagHandler handler, NametagManager manager) {
		super();
		this.handler = handler;
		this.manager = manager;
	}

	@Override
	public void applyTags() {
		handler.applyTags();
	}

	@Override
	public void applyTagToPlayer(Player player, boolean loggedIn) {
		handler.applyTagToPlayer(player, loggedIn);
	}

	@Override
	public void clearNametag(Player player) {
		if (shouldFireEvent(player, NametagEvent.ChangeType.CLEAR)) {
			manager.reset(player.getName());
		}
	}

	@Override
	public void clearNametag(String player) {
		manager.reset(player);
	}

	@Override
	public FakeTeam getFakeTeam(Player player) {
		return manager.getFakeTeam(player.getName());
	}

	@Override
	public List<GroupData> getGroupData() {
		return handler.getGroupData();
	}

	@Override
	public Nametag getNametag(Player player) {
		FakeTeam team = manager.getFakeTeam(player.getName());
		boolean nullTeam = team == null;
		return new Nametag(nullTeam ? "" : team.getPrefix(), nullTeam ? "" : team.getSuffix());
	}

	@Override
	public void reloadNametag(Player player) {
		if (shouldFireEvent(player, NametagEvent.ChangeType.RELOAD)) {
			handler.applyTagToPlayer(player, false);
		}
	}

	@Override
	public void setNametag(OlympaPlayer olympaPlayer) {
		manager.setNametag(olympaPlayer.getName(), olympaPlayer.getGroupPrefix(), null, olympaPlayer.getGroup().getPower());
	}

	@Override
	public void setNametag(Player player, String prefix, String suffix) {
		setNametagAlt(player, prefix, suffix);
	}

	@Override
	public void setNametag(String player, String prefix, String suffix) {
		manager.setNametag(player, prefix, suffix);
	}

	/**
	 * Private helper function to reduce redundancy
	 */
	private void setNametagAlt(Player player, String prefix, String suffix) {
		Nametag nametag = new Nametag(
				handler.format(player, prefix, true),
				handler.format(player, suffix, true));

		NametagEvent event = new NametagEvent(player.getName(), prefix, nametag, NametagEvent.ChangeType.UNKNOWN);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}
		manager.setNametag(player.getName(), nametag.getPrefix(), nametag.getSuffix());
	}

	@Override
	public void setPrefix(Player player, String prefix) {
		FakeTeam fakeTeam = manager.getFakeTeam(player.getName());
		setNametagAlt(player, prefix, fakeTeam == null ? null : fakeTeam.getSuffix());
	}

	@Override
	public void setPrefix(String player, String prefix) {
		FakeTeam fakeTeam = manager.getFakeTeam(player);
		manager.setNametag(player, prefix, fakeTeam == null ? null : fakeTeam.getSuffix());
	}

	@Override
	public void setSuffix(Player player, String suffix) {
		FakeTeam fakeTeam = manager.getFakeTeam(player.getName());
		setNametagAlt(player, fakeTeam == null ? null : fakeTeam.getPrefix(), suffix);
	}

	@Override
	public void setSuffix(String player, String suffix) {
		FakeTeam fakeTeam = manager.getFakeTeam(player);
		manager.setNametag(player, fakeTeam == null ? null : fakeTeam.getPrefix(), suffix);
	}

	/**
	 * Private helper function to reduce redundancy
	 */
	private boolean shouldFireEvent(Player player, NametagEvent.ChangeType type) {
		NametagEvent event = new NametagEvent(player.getName(), "", getNametag(player), type);
		Bukkit.getPluginManager().callEvent(event);
		return !event.isCancelled();
	}

}