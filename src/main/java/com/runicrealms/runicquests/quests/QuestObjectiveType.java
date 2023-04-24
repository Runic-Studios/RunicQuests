package com.runicrealms.runicquests.quests;

/**
 * Contains the different types of quest objectives
 */
public enum QuestObjectiveType {
    CAST("cast"), // Cast a spell
    GATHER("gather"), // Gathering a resource
    LOCATION("location"), // Reaching a location
    SLAY("slay"), // Killing mobs
    TALK("talk"), // Speaking with npcs
    TRIGGER("trigger"); // Handled by commands, run from anywhere

    private final String identifier;

    QuestObjectiveType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

}
