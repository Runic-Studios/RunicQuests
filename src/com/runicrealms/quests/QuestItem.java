package com.runicrealms.quests;

public class QuestItem {
	
	private String itemName;
	private String itemType;
	
	public QuestItem(String itemName, String itemType) {
		this.itemName = itemName;
		this.itemType = itemType;
	}
	
	public String getName() {
		return this.itemName;
	}
	
	public String getType() {
		return this.itemType;
	}
	
}
