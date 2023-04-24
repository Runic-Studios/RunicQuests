package com.runicrealms.runicquests.quests.objective;

import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.List;

/**
 * Abstract class that contains methods that must exist for a quest objective
 */
public abstract class QuestObjective implements Cloneable {
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

    public abstract QuestObjective clone();

    public void executeCommand(String playerName) {
        for (String command : this.execute) {
            String parsedCommand = command.startsWith("/") ? command.substring(1).replaceAll("%player%", playerName) : command.replaceAll("%player%", playerName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
        }
    }

    public List<String> getCompletedMessage() {
        return completedMessage;
    }

    public String getGoalLocation() {
        return this.goalLocation;
    }

    public String getGoalMessage() {
        return this.goalMessage;
    }

    public Integer getObjectiveNumber() {
        return this.objectiveNumber;
    }

    public QuestObjectiveType getObjectiveType() {
        return this.objectiveType;
    }

    public List<QuestItem> getQuestItems() {
        return this.questItems;
    }

    public boolean hasCompletedMessage() {
        return this.completedMessage != null;
    }

    public boolean hasExecute() {
        return this.execute != null;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean requiresQuestItem() {
        return this.questItems != null;
    }

    /**
     * Base method to reset objective for repeatable quests
     */
    public abstract void resetObjective();

    public boolean shouldDisplayNextObjectiveTitle() {
        return this.displayNextTitle;
    }

    /**
     * Writes a perk to mongo. Override in child methods
     *
     * @param source   the objective to write
     * @param document the document to modify
     * @return the modified document
     */
    @SuppressWarnings("unused")
    public Document writeToDocument(QuestObjective source, Document document) {
        document.put("objectiveNumber", source.getObjectiveNumber());
        document.put("completed", source.isCompleted());
        return document;
    }

}
