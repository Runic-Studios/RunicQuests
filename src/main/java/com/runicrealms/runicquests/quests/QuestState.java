package com.runicrealms.runicquests.quests;

/**
 * This class just represents the specific user info that must come with a quest
 */
public class QuestState {
    private boolean completed = false;
    private boolean started = false;

    @SuppressWarnings("unused")
    public QuestState() {
        // Default constructor for Spring
    }

    public QuestState(boolean completed, boolean started) {
        this.setCompleted(completed);
        this.setStarted(started);
    }

    public QuestState(QuestState questState) {
        this.completed = questState.completed;
        this.started = questState.started;
    }

    public boolean hasStarted() {
        return started;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

}
