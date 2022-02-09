package com.runicrealms.runicquests.quests;

/**
 * Contains the different types of quest objectives
 */
public enum QuestObjectiveType {

    SLAY("slay"), // killing mobs
    TALK("talk"), // speaking with npcs
    LOCATION("location"), // reaching a location
    BREAK("break"), // breaking a block
    TRIGGER("trigger");

    private final String identifier;

    private QuestObjectiveType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

}
