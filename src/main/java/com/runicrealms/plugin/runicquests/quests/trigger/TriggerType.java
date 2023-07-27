package com.runicrealms.plugin.runicquests.quests.trigger;

public enum TriggerType {
    ANY("Any"),
    ALL("All");

    private final String identifier;

    TriggerType(String identifier) {
        this.identifier = identifier;
    }

    public static TriggerType getFromIdentifier(String identifier) {
        for (TriggerType triggerType : TriggerType.values()) {
            if (triggerType.getIdentifier().equalsIgnoreCase(identifier)) {
                return triggerType;
            }
        }
        return null;
    }

    public String getIdentifier() {
        return identifier;
    }
}
