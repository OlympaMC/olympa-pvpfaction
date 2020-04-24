package fr.olympa.pvpfac.factions.scoreboard.api.data;

import java.util.UUID;

/**
 * This class represents a player nametag. There are several properties
 * available.
 */
public class PlayerData implements INametag {

	private String name;
	private UUID uuid;
	private String prefix;
	private String suffix;
	private int sortPriority;

	public PlayerData(String name, UUID uuid, String prefix, String suffix, int sortPriority) {
		super();
		this.name = name;
		this.uuid = uuid;
		this.prefix = prefix;
		this.suffix = suffix;
		this.sortPriority = sortPriority;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public int getSortPriority() {
		return sortPriority;
	}

	@Override
	public String getSuffix() {
		return suffix;
	}

	public UUID getUuid() {
		return uuid;
	}

	@Override
	public boolean isPlayerTag() {
		return true;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setSortPriority(int sortPriority) {
		this.sortPriority = sortPriority;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

}