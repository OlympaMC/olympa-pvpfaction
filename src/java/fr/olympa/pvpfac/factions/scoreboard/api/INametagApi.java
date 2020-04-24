package fr.olympa.pvpfac.factions.scoreboard.api;

import java.util.List;

import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.pvpfac.factions.scoreboard.api.data.FakeTeam;
import fr.olympa.pvpfac.factions.scoreboard.api.data.GroupData;
import fr.olympa.pvpfac.factions.scoreboard.api.data.Nametag;

/**
 *
 */
public interface INametagApi {

	/**
	 * Applies tags to all players
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 * </p>
	 */
	void applyTags();

	/**
	 * Applies tags to specific player
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage
	 * </p>
	 *
	 * @param player   the player to apply nametag to
	 * @param loggedIn is the player logged in
	 */
	void applyTagToPlayer(Player player, boolean loggedIn);

	/**
	 * Removes a player's nametag in memory only.
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 *
	 * @param player whose nametag to clear
	 */
	void clearNametag(Player player);

	/**
	 * Removes a player's nametag in memory only.
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 *
	 * @param player whose nametag to clear
	 */
	void clearNametag(String player);

	/**
	 * Function gets the fake team data for player.
	 *
	 * @param player the player to check
	 * @return the fake team
	 */
	FakeTeam getFakeTeam(Player player);

	/**
	 * Gets the data of all groups
	 *
	 * @return list containing all group data
	 */
	List<GroupData> getGroupData();

	/**
	 * Function gets the nametag for a player if it exists. This will never return a
	 * null.
	 *
	 * @param player the player to check
	 * @return the nametag for the player
	 */
	Nametag getNametag(Player player);

	/**
	 * Reloads a nametag if the player has a custom nametag via the Players or
	 * Groups configurations.
	 * <p>
	 *
	 * @param player whose nametag to reload
	 */
	void reloadNametag(Player player);

	void setNametag(OlympaPlayer olympaPlayer);

	/**
	 * Sets the nametag for a player.
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 *
	 * @param player the player whose nametag to change
	 * @param prefix the prefix to change to
	 * @param suffix the suffix to change to
	 */
	void setNametag(Player player, String prefix, String suffix);

	/**
	 * Sets the nametag for a player.
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 *
	 * @param player the player whose nametag to change
	 * @param prefix the prefix to change to
	 * @param suffix the suffix to change to
	 */
	void setNametag(String player, String prefix, String suffix);

	/**
	 * Sets the prefix for a player. The previous suffix is kept if it exists.
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 *
	 * @param player the player whose nametag to change
	 * @param prefix the prefix to change to
	 */
	void setPrefix(Player player, String prefix);

	/**
	 * Sets the prefix for a player. The previous suffix is kept if it exists.
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 *
	 * @param player the player whose nametag to change
	 * @param prefix the prefix to change to
	 */
	void setPrefix(String player, String prefix);

	/**
	 * Sets the suffix for a player. The previous prefix is kept if it exists.
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 *
	 * @param player the player whose nametag to change
	 * @param suffix the suffix to change to
	 */
	void setSuffix(Player player, String suffix);

	/**
	 * Sets the suffix for a player. The previous prefix is kept if it exists.
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 *
	 * @param player the player whose nametag to change
	 * @param suffix the suffix to change to
	 */
	void setSuffix(String player, String suffix);
}