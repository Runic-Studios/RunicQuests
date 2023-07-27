package com.runicrealms.plugin.runicquests.quests;

import java.util.List;

public class QuestIdleMessageConditions {
	
	/*
	 * Represents a quest NPC's idle message's conditions.
	 * None of these are required. If they don't exist, they will be ignored
	 */
	
	private Boolean questStarted;
	private Boolean questCompleted;
	private List<Boolean> objectiveStates;
	private Boolean questItems;
	
	public QuestIdleMessageConditions(Boolean questStarted, Boolean questCompleted, List<Boolean> objectiveStates, Boolean questItems) {
		this.questCompleted = questCompleted;
		this.questStarted = questStarted;
		this.objectiveStates = objectiveStates;
		this.questItems = questItems;
	}
	
	public Boolean getQuestStarted() {
		return this.questStarted;
	}
	
	public Boolean getQuestCompleted() {
		return this.questCompleted;
	}
	
	public List<Boolean> getObjectiveStates() {
		return this.objectiveStates;
	}
	
	public Boolean requiresQuestItems() {
		return this.questItems;
	}
	
	public boolean hasQuestStarted() {
		return this.questStarted != null;
	}
	
	public boolean hasQuestCompleted() {
		return this.questCompleted != null;
	}
	
	public boolean hasObjectiveStates() {
		return this.objectiveStates != null;
	}
	
	public boolean hasRequiresQuestItems() {
		return this.questItems != null;
	}
	
}
