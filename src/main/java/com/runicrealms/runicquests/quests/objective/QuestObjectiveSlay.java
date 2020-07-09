package com.runicrealms.runicquests.quests.objective;

import java.util.List;

import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;

public class QuestObjectiveSlay extends QuestObjective {
	
	/*
	 * Slay quest objective
	 */
	
	private final List<String> mobNames;
	private final Integer mobAmount;
	private Integer mobsKilled = 0;
	
	public QuestObjectiveSlay(List<String> mobNames, Integer mobAmount, List<QuestItem> questItems, String goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage, String goalLocation) {
		super(objectiveNumber, QuestObjectiveType.SLAY, completedMessage, questItems, goalMessage, execute, goalLocation);
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

	@Override
	public QuestObjectiveSlay clone() {
		return new QuestObjectiveSlay(this.mobNames, this.mobAmount, this.questItems, this.goalMessage, this.execute, this.objectiveNumber, this.completedMessage, this.goalLocation);
	}
	
}
