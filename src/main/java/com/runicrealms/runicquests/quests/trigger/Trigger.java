package com.runicrealms.runicquests.quests.trigger;

public class Trigger {

    private int questId;
    private int objectiveId;

    public Trigger(int questId, int objectiveId) {
        this.questId = questId;
        this.objectiveId = objectiveId;
    }

    public int getQuestId() {
        return this.questId;
    }

    public int getObjectiveId() {
        return this.objectiveId;
    }

}
