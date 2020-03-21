package com.runicrealms.runicquests.quests.objective;

import java.util.List;

import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.location.LocationToReach;

public class QuestObjectiveLocation extends QuestObjective {
	
	/*
	 * Location quest objective
	 */
	
	private LocationToReach location;
	
	public QuestObjectiveLocation(LocationToReach location, List<QuestItem> questItems, String goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		super(objectiveNumber, QuestObjectiveType.LOCATION, completedMessage, questItems, goalMessage, execute);
		this.location = location;
	}
	
	public LocationToReach getLocation() {
		return this.location;
	}

	@Override
	public QuestObjectiveLocation clone() {
		return new QuestObjectiveLocation(this.location, this.questItems, this.goalMessage, this.execute, this.objectiveNumber, this.completedMessage);
	}
	
}
