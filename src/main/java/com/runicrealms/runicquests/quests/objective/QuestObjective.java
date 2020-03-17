package com.runicrealms.runicquests.quests.objective;

import java.util.List;

import org.bukkit.Bukkit;

import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;

public abstract class QuestObjective {
	
	/*
	 * Abstract class that contains methods that must exist for a quest objective
	 */
	
	protected Integer objectiveNumber;
	protected QuestObjectiveType objectiveType;
	protected List<String> completedMessage;
	protected List<QuestItem> questItems;
	protected String goalMessage;
	protected List<String> execute;
	
	private boolean completed = false;
	
	public QuestObjective(Integer objectiveNumber, QuestObjectiveType objectiveType, List<String> completedMessage, List<QuestItem> questItems, String goalMessage, List<String> execute) {
		this.objectiveNumber = objectiveNumber;
		this.objectiveType = objectiveType;
		this.completedMessage = completedMessage;
		this.questItems = questItems;
		this.goalMessage = goalMessage;
		this.execute = execute;
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
	
	public void executeCommand(String playerName) {
		for (String command : this.execute) {
			String parsedCommand = command.startsWith("/") ? command.substring(1).replaceAll("%player%", playerName) : command.replaceAll("%player%", playerName);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
		}
	}
	
	public static QuestObjective getObjective(List<QuestObjective> objectives, Integer objectiveNumber) {
		for (QuestObjective objective : objectives) {
			if (objective.objectiveNumber == objectiveNumber) {
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
	
}
