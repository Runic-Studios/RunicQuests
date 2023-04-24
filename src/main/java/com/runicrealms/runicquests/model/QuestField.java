package com.runicrealms.runicquests.model;

public enum QuestField {

    COMPLETED("completed"),
    DATE_COMPLETED("date-completed"),
    FIRST_NPC_STATE("first-npc-state"),
    STARTED("started");

    private final String field;

    QuestField(String field) {
        this.field = field;
    }

    /**
     * Returns the corresponding RedisField from the given string version
     *
     * @param field a string matching a constant
     * @return the constant
     */
    public static QuestField getFromFieldString(String field) {
        for (QuestField questField : QuestField.values()) {
            if (questField.getField().equalsIgnoreCase(field))
                return questField;
        }
        return null;
    }

    public String getField() {
        return field;
    }
}
