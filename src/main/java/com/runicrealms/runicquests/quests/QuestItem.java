package com.runicrealms.runicquests.quests;

public class QuestItem {
	
	/*
	 * Represents a required quest item for an objective
	 */
	
	private String itemName;
	private String itemType;
	private int amount;
	
	public QuestItem(String itemName, String itemType, int amount) {
		this.itemName = itemName;
		this.itemType = itemType;
		this.amount = amount;
	}

	public String getItemName() {
		return itemName;
	}

	public String getItemType() {
		return itemType;
	}
	
	public int getAmount() {
		return amount;
	}

}