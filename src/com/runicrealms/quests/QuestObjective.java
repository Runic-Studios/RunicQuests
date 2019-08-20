package com.runicrealms.quests;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import com.runicrealms.player.QuestObjectiveState;

public class QuestObjective {
	
	private Integer objectiveNumber;
	private QuestObjectiveType objectiveType;
	private List<String> completedMessage;
	private QuestItem questItem = null;
	private String mobName;
	private Integer mobAmount;
	private Integer mobsKilled = 0;
	private QuestNpc questNpc;
	private Location tripwire1;
	private Location tripwire2;
	private Material blockMaterial;
	private List<String> goalMessage;
	private List<String> execute;
	
	private boolean completed = false;
	
	public QuestObjective(String mobName, Integer mobAmount, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		this.mobName = mobName;
		this.mobAmount = mobAmount;
		this.goalMessage = goalMessage;
		this.objectiveType = QuestObjectiveType.SLAY;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
		this.completedMessage = completedMessage;
	}
	
	public QuestObjective(QuestNpc questNpc, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		this.questNpc = questNpc;
		this.goalMessage = goalMessage;
		this.objectiveType = QuestObjectiveType.TALK;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
		this.completedMessage = completedMessage;
	}
	
	public QuestObjective(Location tripwire1, Location tripwire2, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		this.tripwire1 = tripwire1;
		this.tripwire2 = tripwire2;
		this.goalMessage = goalMessage;
		this.objectiveType = QuestObjectiveType.TRIPWIRE;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
		this.completedMessage = completedMessage;
	}
	
	public QuestObjective(Material blockMaterial, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		this.blockMaterial = blockMaterial;
		this.goalMessage = goalMessage;
		this.objectiveType = QuestObjectiveType.BREAK;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
		this.completedMessage = completedMessage;
	}
	
	public QuestObjective(String mobName, Integer mobAmount, QuestItem questItem, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		this.mobName = mobName;
		this.mobAmount = mobAmount;
		this.goalMessage = goalMessage;
		this.questItem = questItem;
		this.objectiveType = QuestObjectiveType.SLAY;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
		this.completedMessage = completedMessage;
	}
	
	public QuestObjective(QuestNpc questNpc, QuestItem questItem, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		this.questNpc = questNpc;
		this.goalMessage = goalMessage;
		this.questItem = questItem;
		this.objectiveType = QuestObjectiveType.TALK;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
		this.completedMessage = completedMessage;
	}
	
	public QuestObjective(Location tripwire1, Location tripwire2, QuestItem questItem, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		this.tripwire1 = tripwire1;
		this.tripwire2 = tripwire2;
		this.goalMessage = goalMessage;
		this.questItem = questItem;
		this.objectiveType = QuestObjectiveType.TRIPWIRE;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
		this.completedMessage = completedMessage;
	}
	
	public QuestObjective(Material blockMaterial, QuestItem questItem, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		this.blockMaterial = blockMaterial;
		this.goalMessage = goalMessage;
		this.questItem = questItem;
		this.objectiveType = QuestObjectiveType.BREAK;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
		this.completedMessage = completedMessage;
	}
	
	public QuestItem getQuestItem() {
		return this.questItem;
	}
	
	public Integer getObjectiveNumber() {
		return this.objectiveNumber;
	}
	
	public QuestObjectiveType getObjectiveType() {
		return this.objectiveType;
	}
	
	public String getMobName() {
		return this.mobName;
	}
	
	public Integer getMobAmount() {
		return this.mobAmount;
	}
	
	public Integer getMobsKilled() {
		return this.mobsKilled;
	}
	
	public void setMobsKilled(Integer mobsKilled) {
		this.mobsKilled = mobsKilled;
	}
	
	public QuestNpc getQuestNpc() {
		return this.questNpc;
	}
	
	public Location getTripwire1() {
		return this.tripwire1;
	}
	
	public Location getTripwire2() {
		return this.tripwire2;
	}
	
	public Material getBlockMaterial() {
		return this.blockMaterial;
	}
	
	public List<String> getGoalMessage() {
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
		return this.questItem != null;
	}
	
	public boolean hasExecute() {
		return this.execute != null;
	}
	
	public boolean hasCompletedMessage() {
		return this.completedMessage != null;
	}
	
	public void executeCommand(String playerName) {
		for (String command : this.execute) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", playerName));
		}
	}
	
	public static QuestObjective getObjective(HashMap<QuestObjective, QuestObjectiveState> objectives, Integer objectiveNumber) {
		for (QuestObjective objective : objectives.keySet()) {
			if (objective.objectiveNumber == objectiveNumber) {
				return objective;
			}
		}
		return null;
	}
	
	public static QuestObjective getLastObjective(HashMap<QuestObjective, QuestObjectiveState> objectives) {
		for (QuestObjective objective : objectives.keySet()) {
			if (objective.objectiveNumber == objectives.keySet().size()) {
				return objective;
			}
		}
		return null;
	}
	
}
