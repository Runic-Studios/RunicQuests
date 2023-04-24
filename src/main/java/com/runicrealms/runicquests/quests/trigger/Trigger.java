package com.runicrealms.runicquests.quests.trigger;

public class Trigger {
    private final String triggerId;
    private final int questId;
    private final int objectiveId;

    /**
     * Represents a trigger, which can be run as a command
     *
     * @param triggerId   of the trigger (should be unique, e.g. punch-azana-scarecrow)
     * @param questId     of the quest trigger belongs to
     * @param objectiveId of the objective trigger belongs to
     */
    public Trigger(String triggerId, int questId, int objectiveId) {
        this.triggerId = triggerId;
        this.questId = questId;
        this.objectiveId = objectiveId;
    }

    public int getObjectiveId() {
        return this.objectiveId;
    }

    public int getQuestId() {
        return this.questId;
    }

    public String getTriggerId() {
        return triggerId;
    }

}
