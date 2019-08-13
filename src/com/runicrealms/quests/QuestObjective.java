package com.runicrealms.quests;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;

public class QuestObjective {
	
	public QuestObjectiveType objectiveType;
	public QuestItem questItem = null;
	public String mobName;
	public Integer mobAmount;
	public QuestNpc questNpc;
	public Location tripwire1;
	public Location tripwire2;
	public Material blockMaterial;
	public Location blockLocation;
	public List<String> goalMessage;
	
	public boolean completed = false;
	
	public QuestObjective(String mobName, Integer mobAmount, List<String> goalMessage) {
		this.mobName = mobName;
		this.mobAmount = mobAmount;
		this.goalMessage = goalMessage;
		this.objectiveType = QuestObjectiveType.SLAY;
	}
	
	public QuestObjective(QuestNpc questNpc, List<String> goalMessage) {
		this.questNpc = questNpc;
		this.goalMessage = goalMessage;
		this.objectiveType = QuestObjectiveType.TALK;
	}
	
	public QuestObjective(Location tripwire1, Location tripwire2, List<String> goalMessage) {
		this.tripwire1 = tripwire1;
		this.tripwire2 = tripwire2;
		this.goalMessage = goalMessage;
		this.objectiveType = QuestObjectiveType.TRIPWIRE;
	}
	
	public QuestObjective(Material blockMaterial, Location blockLocation, List<String> goalMessage) {
		this.blockMaterial = blockMaterial;
		this.blockLocation = blockLocation;
		this.goalMessage = goalMessage;
		this.objectiveType = QuestObjectiveType.BREAK;
	}
	
	public QuestObjective(String mobName, Integer mobAmount, QuestItem questItem, List<String> goalMessage) {
		this.mobName = mobName;
		this.mobAmount = mobAmount;
		this.goalMessage = goalMessage;
		this.questItem = questItem;
		this.objectiveType = QuestObjectiveType.SLAY;
	}
	
	public QuestObjective(QuestNpc questNpc, QuestItem questItem, List<String> goalMessage) {
		this.questNpc = questNpc;
		this.goalMessage = goalMessage;
		this.questItem = questItem;
		this.objectiveType = QuestObjectiveType.TALK;
	}
	
	public QuestObjective(Location tripwire1, Location tripwire2, QuestItem questItem, List<String> goalMessage) {
		this.tripwire1 = tripwire1;
		this.tripwire2 = tripwire2;
		this.goalMessage = goalMessage;
		this.questItem = questItem;
		this.objectiveType = QuestObjectiveType.TRIPWIRE;
	}
	
	public QuestObjective(Material blockMaterial, Location blockLocation, QuestItem questItem, List<String> goalMessage) {
		this.blockMaterial = blockMaterial;
		this.blockLocation = blockLocation;
		this.goalMessage = goalMessage;
		this.questItem = questItem;
		this.objectiveType = QuestObjectiveType.BREAK;
	}
	
	public QuestItem getQuestItem() {
		return this.questItem;
	}
	
	public boolean requiresQuesItem() {
		return this.questItem != null;
	}
	
}
