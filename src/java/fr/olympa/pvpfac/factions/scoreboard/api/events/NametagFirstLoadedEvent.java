package fr.olympa.pvpfac.factions.scoreboard.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.pvpfac.factions.scoreboard.api.data.INametag;

/**
 * This class represents an Event that is fired when a player joins the server
 * and receives their nametag.
 */
public class NametagFirstLoadedEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	private Player player;

	private INametag nametag;

	public NametagFirstLoadedEvent(Player player, INametag nametag) {
		super();
		this.player = player;
		this.nametag = nametag;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public INametag getNametag() {
		return nametag;
	}

	public Player getPlayer() {
		return player;
	}

}