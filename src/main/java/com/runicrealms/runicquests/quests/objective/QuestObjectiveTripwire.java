package com.runicrealms.runicquests.quests.objective;

import java.util.List;

import com.runicrealms.runicquests.quests.ObjectiveTripwire;
import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;

public class QuestObjectiveTripwire extends QuestObjective {
	
	/*
	 * Tripwire quest objective
	 */
	
	private List<ObjectiveTripwire> tripwires;
	
	public QuestObjectiveTripwire(List<ObjectiveTripwire> tripwires, String goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		super(objectiveNumber, QuestObjectiveType.TRIPWIRE, completedMessage, goalMessage, execute);
		this.tripwires = tripwires;
	}
	
	public QuestObjectiveTripwire(List<ObjectiveTripwire> tripwires, List<QuestItem> questItems, String goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		super(objectiveNumber, QuestObjectiveType.TRIPWIRE, completedMessage, questItems, goalMessage, execute);
		this.tripwires = tripwires;
	}
	
	public List<ObjectiveTripwire> getTripwires() {
		return this.tripwires;
	}
	
}