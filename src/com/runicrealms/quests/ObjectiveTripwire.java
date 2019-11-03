package com.runicrealms.quests;

import org.bukkit.Location;

public class ObjectiveTripwire {
	
	/*
	 * Represents a tripwire with it's two corner locations
	 */
	
	private Location corner1;
	private Location corner2;
	
	public ObjectiveTripwire(Location corner1, Location corner2) {
		this.corner1 = corner1;
		this.corner2 = corner2;
	}
	
	public Location getCorner1() {
		return this.corner1;
	}
	
	public Location getCorner2() {
		return this.corner2;
	}
	
}
