package com.runicrealms.quests.objective;

import java.util.List;

import com.runicrealms.quests.QuestItem;
import com.runicrealms.quests.QuestObjectiveType;

public class QuestObjectiveSlay extends QuestObjective {
	
	private List<String> mobNames;
	private Integer mobAmount;
	private Integer mobsKilled = 0;
	
	public QuestObjectiveSlay(List<String> mobNames, Integer mobAmount, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		super(objectiveNumber, QuestObjectiveType.SLAY, completedMessage, goalMessage, execute);
		this.mobNames = mobNames;
		this.mobAmount = mobAmount;
	}
	
	public QuestObjectiveSlay(List<String> mobNames, Integer mobAmount, List<QuestItem> questItems, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		super(objectiveNumber, QuestObjectiveType.SLAY, completedMessage, questItems, goalMessage, execute);
		this.mobNames = mobNames;
		this.mobAmount = mobAmount;
	}
	
	public List<String> getMobNames() {
		return this.mobNames;
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
	
}
