package com.runicrealms.plugin.runicquests.quests.objective;

import com.runicrealms.plugin.runicquests.quests.location.LocationToReach;
import com.runicrealms.plugin.runicquests.quests.QuestItem;
import com.runicrealms.plugin.runicquests.quests.QuestObjectiveType;

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
