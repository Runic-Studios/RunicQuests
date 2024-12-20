package com.runicrealms.plugin.runicquests.model;

import com.runicrealms.plugin.runicquests.quests.objective.QuestObjective;

public class QuestObjectiveDTO {
    private boolean completed;

    @SuppressWarnings("unused")
    public QuestObjectiveDTO() {
        // Default constructor for Spring
    }

    public QuestObjectiveDTO(QuestObjective objective) {
        this.completed = objective.isCompleted();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
