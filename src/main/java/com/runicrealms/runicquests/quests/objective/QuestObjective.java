package com.runicrealms.runicquests.quests.objective;

import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import org.bukkit.Bukkit;

import java.util.List;

public abstract class QuestObjective implements Cloneable {

    /*
     * Abstract class that contains methods that must exist for a quest objective
     */

    protected Integer objectiveNumber;
    protected QuestObjectiveType objectiveType;
    protected List<String> completedMessage;
    protected List<QuestItem> questItems;
    protected String goalMessage;
    protected List<String> execute;
    protected String goalLocation;
    protected boolean displayNextTitle;

    private boolean completed = false;

    public QuestObjective(Integer objectiveNumber, QuestObjectiveType objectiveType, List<String> completedMessage, List<QuestItem> questItems, String goalMessage, List<String> execute, String goalLocation, boolean displayNextTitle) {
        this.objectiveNumber = objectiveNumber;
        this.objectiveType = objectiveType;
        this.completedMessage = completedMessage;
        this.questItems = questItems;
        this.goalMessage = goalMessage;
        this.execute = execute;
        this.goalLocation = goalLocation;
        this.displayNextTitle = displayNextTitle;
    }

    public static QuestObjective getObjective(List<QuestObjective> objectives, Integer objectiveNumber) {
        for (QuestObjective objective : objectives) {
            if (objective.objectiveNumber.equals(objectiveNumber)) {
                return objective;
            }
        }
        return null;
    }

    public static QuestObjective getLastObjective(List<QuestObjective> objectives) {
        for (QuestObjective objective : objectives) {
            if (objective.objectiveNumber == objectives.size()) {
                return objective;
            }
        }
        return null;
    }

    public List<QuestItem> getQuestItems() {
        return this.questItems;
    }

    public Integer getObjectiveNumber() {
        return this.objectiveNumber;
    }

    public QuestObjectiveType getObjectiveType() {
        return this.objectiveType;
    }

    public String getGoalMessage() {
        return this.goalMessage;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public List<String> getCompletedMessage() {
        return completedMessage;
    }

    public boolean requiresQuestItem() {
        return this.questItems != null;
    }

    public boolean hasExecute() {
        return this.execute != null;
    }

    public boolean hasCompletedMessage() {
        return this.completedMessage != null;
    }

    public String getGoalLocation() {
        return this.goalLocation;
    }

    public boolean shouldDisplayNextObjectiveTitle() {
        return this.displayNextTitle;
    }

    public void executeCommand(String playerName) {
        for (String command : this.execute) {
            String parsedCommand = command.startsWith("/") ? command.substring(1).replaceAll("%player%", playerName) : command.replaceAll("%player%", playerName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
        }
    }

    public abstract QuestObjective clone();

}
