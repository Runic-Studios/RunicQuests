package com.runicrealms.quests;

public enum CraftingProfessionType {

	JEWELING("jeweling"), ALCHEMY("alchemy"), BLACKSMITHING("blacksmithing"), TAILORING("tailoring"), LEATHERWORKING("leatherworking"), ANY("crafting");
	
	private String name;
	
	private CraftingProfessionType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
}
