package fr.olympa.pvpfac.factions.scoreboard.api.data;

public interface INametag {
	String getPrefix();

	int getSortPriority();

	String getSuffix();

	boolean isPlayerTag();
}