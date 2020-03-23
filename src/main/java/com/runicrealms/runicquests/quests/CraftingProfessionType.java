package com.runicrealms.runicquests.quests;

public enum CraftingProfessionType {

	/*
	 * Represents the crafting professions
	 */
	
	ENCHANTER("enchanter"), HUNTER("hunter"), BLACKSMITH("blacksmith"), JEWELER("jeweler"), ALCHEMIST("alchemist"), ANY("crafting");
	
	private String name;
	
	private CraftingProfessionType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
}
