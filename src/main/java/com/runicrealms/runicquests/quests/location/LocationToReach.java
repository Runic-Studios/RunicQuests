package com.runicrealms.runicquests.quests.location;

import org.bukkit.entity.Player;

public interface LocationToReach {

	/**
	 * @return the type of location. Box, or radius
	 */
	LocationType getLocationType();

	/**
	 * Determines if the player has reached the location objective.
	 * Has differently logic depending on the type of location (box, radius, etc.)
	 *
	 * @param player whose location will be checked
	 * @return true if they have reached location
	 */
	boolean hasReachedLocation(Player player);

}
