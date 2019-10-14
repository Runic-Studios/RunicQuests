package com.runicrealms.quests;

public enum CraftingProfessionType {

	ENCHANTER("enchanter"), HUNTER("hunter"), BLACKSMITH("blacksmith"), ANY("crafting");
	
	private String name;
	
	private CraftingProfessionType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
}
