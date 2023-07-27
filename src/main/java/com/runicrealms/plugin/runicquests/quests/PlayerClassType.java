package com.runicrealms.plugin.runicquests.quests;

public enum PlayerClassType {

	/*
	 * Represents the class types
	 */
	
	WARRIOR("warrior"), CLERIC("cleric"), MAGE("mage"), ROGUE("rogue"), ARCHER("archer");
	
	private String name;
	
	private PlayerClassType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public static PlayerClassType getFromString(String name) {
		for (PlayerClassType classType : PlayerClassType.values()) {
			if (classType.getName().equalsIgnoreCase(name)) {
				return classType;
			}
		}
		return null;
	}
	
}
