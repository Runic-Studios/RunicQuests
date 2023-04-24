package com.runicrealms.runicquests.quests;

/**
 * Represents a required quest item for an objective
 */
public class QuestItem {
    private final String itemName;
    private final String itemType;
    private final int amount;

    public QuestItem(String itemName, String itemType, int amount) {
        this.itemName = itemName;
        this.itemType = itemType;
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemType() {
        return itemType;
    }

}
