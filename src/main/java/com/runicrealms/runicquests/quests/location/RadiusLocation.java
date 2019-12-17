package com.runicrealms.runicquests.quests.location;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RadiusLocation implements LocationToReach {

	private Location location;
	private Integer radius;

	public RadiusLocation(Location location, Integer radius) {
		this.location = location;
		this.radius = radius;
	}

	@Override
	public LocationType getLocationType() {
		return LocationType.RADIUS;
	}

	@Override
	public boolean hasReachedLocation(Player player) {
		if (!player.getWorld().toString().equalsIgnoreCase(location.getWorld().toString())) {
			return false;
		}
		return this.location.distanceSquared(player.getLocation()) <= this.radius * this.radius;
	}

}
