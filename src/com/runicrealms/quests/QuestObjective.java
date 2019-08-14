package com.runicrealms.quests;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class QuestObjective {
	
	public Integer objectiveNumber;
	public QuestObjectiveType objectiveType;
	public QuestItem questItem = null;
	public String mobName;
	public Integer mobAmount;
	public QuestNpc questNpc;
	public Location tripwire1;
	public Location tripwire2;
	public Material blockMaterial;
	public List<String> goalMessage;
	public List<String> execute;
	
	public boolean completed = false;
	
	public QuestObjective(String mobName, Integer mobAmount, List<String> goalMessage, List<String> execute, Integer objectiveNumber) {
		this.mobName = mobName;
		this.mobAmount = mobAmount;
		this.goalMessage = goalMessage;
		this.objectiveType = QuestObjectiveType.SLAY;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
	}
	
	public QuestObjective(QuestNpc questNpc, List<String> goalMessage, List<String> execute, Integer objectiveNumber) {
		this.questNpc = questNpc;
		this.goalMessage = goalMessage;
		this.objectiveType = QuestObjectiveType.TALK;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
	}
	
	public QuestObjective(Location tripwire1, Location tripwire2, List<String> goalMessage, List<String> execute, Integer objectiveNumber) {
		this.tripwire1 = tripwire1;
		this.tripwire2 = tripwire2;
		this.goalMessage = goalMessage;
		this.objectiveType = QuestObjectiveType.TRIPWIRE;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
	}
	
	public QuestObjective(Material blockMaterial, List<String> goalMessage, List<String> execute, Integer objectiveNumber) {
		this.blockMaterial = blockMaterial;
		this.goalMessage = goalMessage;
		this.objectiveType = QuestObjectiveType.BREAK;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
	}
	
	public QuestObjective(String mobName, Integer mobAmount, QuestItem questItem, List<String> goalMessage, List<String> execute, Integer objectiveNumber) {
		this.mobName = mobName;
		this.mobAmount = mobAmount;
		this.goalMessage = goalMessage;
		this.questItem = questItem;
		this.objectiveType = QuestObjectiveType.SLAY;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
	}
	
	public QuestObjective(QuestNpc questNpc, QuestItem questItem, List<String> goalMessage, List<String> execute, Integer objectiveNumber) {
		this.questNpc = questNpc;
		this.goalMessage = goalMessage;
		this.questItem = questItem;
		this.objectiveType = QuestObjectiveType.TALK;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
	}
	
	public QuestObjective(Location tripwire1, Location tripwire2, QuestItem questItem, List<String> goalMessage, List<String> execute, Integer objectiveNumber) {
		this.tripwire1 = tripwire1;
		this.tripwire2 = tripwire2;
		this.goalMessage = goalMessage;
		this.questItem = questItem;
		this.objectiveType = QuestObjectiveType.TRIPWIRE;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
	}
	
	public QuestObjective(Material blockMaterial, QuestItem questItem, List<String> goalMessage, List<String> execute, Integer objectiveNumber) {
		this.blockMaterial = blockMaterial;
		this.goalMessage = goalMessage;
		this.questItem = questItem;
		this.objectiveType = QuestObjectiveType.BREAK;
		this.execute = execute;
		this.objectiveNumber = objectiveNumber;
	}
	
	public QuestItem getQuestItem() {
		return this.questItem;
	}
	
	public boolean requiresQuesItem() {
		return this.questItem != null;
	}
	
	public boolean hasExecute() {
		return this.execute != null;
	}
	
	public void executeCommand(String playerName) {
		for (String command : this.execute) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", playerName));
		}
	}
	
}
