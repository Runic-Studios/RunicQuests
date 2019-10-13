package com.runicrealms.quests.objective;

import java.util.List;

import org.bukkit.Location;

import com.runicrealms.quests.QuestItem;
import com.runicrealms.quests.QuestObjectiveType;

public class QuestObjectiveTripwire extends QuestObjective {
	
	private Location tripwire1;
	private Location tripwire2;
	
	public QuestObjectiveTripwire(Location tripwire1, Location tripwire2, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		super(objectiveNumber, QuestObjectiveType.TRIPWIRE, completedMessage, goalMessage, execute);
		this.tripwire1 = tripwire1;
		this.tripwire2 = tripwire2;
	}
	
	public QuestObjectiveTripwire(Location tripwire1, Location tripwire2, List<QuestItem> questItems, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		super(objectiveNumber, QuestObjectiveType.TRIPWIRE, completedMessage, questItems, goalMessage, execute);
		this.tripwire1 = tripwire1;
		this.tripwire2 = tripwire2;
	}
	
	public Location getTripwire1() {
		return this.tripwire1;
	}
	
	public Location getTripwire2() {
		return this.tripwire2;
	}
	
}
