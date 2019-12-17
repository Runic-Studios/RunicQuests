package com.runicrealms.runicquests.quests.location;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BoxLocation implements LocationToReach {
	
	private Location corner1;
	private Location corner2;
	
	public BoxLocation(Location corner1, Location corner2) {
		this.corner1 = new Location(
				corner1.getWorld(),
				Math.min(corner1.getX(), corner2.getX()), 
				Math.min(corner1.getY(), corner2.getY()),
				Math.min(corner1.getZ(), corner2.getZ()));
		this.corner2 = new Location(
				corner2.getWorld(),
				Math.max(corner1.getX(), corner2.getX()), 
				Math.max(corner1.getY(), corner2.getY()),
				Math.max(corner1.getZ(), corner2.getZ()));
	}
	
	@Override
	public LocationType getLocationType() {
		return LocationType.BOX;
	}

	@Override
	public boolean hasReachedLocation(Player player) {
		if (player.getLocation().getX() < corner1.getX()) {
			return false;
		}
		if (player.getLocation().getY() < corner1.getY()) {
			return false;
		}
		if (player.getLocation().getZ() < corner1.getZ()) {
			return false;
		}
		if (player.getLocation().getX() > corner2.getX()) {
			return false;
		}
		if (player.getLocation().getY() > corner2.getY()) {
			return false;
		}
		if (player.getLocation().getZ() > corner2.getZ()) {
			return false;
		}
		return true;
	}

}
