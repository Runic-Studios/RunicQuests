package com.runicrealms.runicquests.quests.objective;

import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.location.LocationToReach;

import java.util.List;

public class QuestObjectiveLocation extends QuestObjective {
	private final LocationToReach location;

	public QuestObjectiveLocation(LocationToReach location, List<QuestItem> questItems, String goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage, String goalLocation, boolean displayNextTitle) {
		super(objectiveNumber, QuestObjectiveType.LOCATION, completedMessage, questItems, goalMessage, execute, goalLocation, displayNextTitle);
		this.location = location;
	}

	@Override
	public QuestObjectiveLocation clone() {
		return new QuestObjectiveLocation(this.location, this.questItems, this.goalMessage, this.execute, this.objectiveNumber, this.completedMessage, this.goalLocation, this.displayNextTitle);
	}

	@Override
	public void resetObjective() {

	}

	public LocationToReach getLocation() {
		return this.location;
	}

}
