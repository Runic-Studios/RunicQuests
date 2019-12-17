package com.runicrealms.runicquests.quests.location;

import org.bukkit.entity.Player;

public interface LocationToReach {
	
	public LocationType getLocationType();
	
	public boolean hasReachedLocation(Player player);
	
}
