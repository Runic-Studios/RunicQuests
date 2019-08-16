package com.runicrealms.quests;

import java.util.ArrayList;
import java.util.HashMap;

import com.runicrealms.player.QuestObjectiveState;
import com.runicrealms.player.QuestState;

public class Quest {
	
	private String questName;
	private QuestFirstNpc firstNPC;
	private HashMap<QuestObjective, QuestObjectiveState> objectives;
	private QuestRewards rewards;
	private QuestState state;
	private Integer questID;
	private QuestRequirements requirements;
	
	public Quest(String questName, QuestFirstNpc firstNPC, ArrayList<QuestObjective> objectives, QuestRewards rewards, Integer questID, QuestRequirements requirements) {
		this.questName = questName;
		this.firstNPC = firstNPC;
		for (QuestObjective objective : objectives) {
			this.objectives.put(objective, new QuestObjectiveState());
		}
		this.rewards = rewards;
		this.state = new QuestState(false, false);
		this.questID = questID;
		this.requirements = requirements;
	}
	
	public Quest(Quest quest) {
		this.questName = quest.questName;
		this.firstNPC = quest.firstNPC;
		this.objectives = quest.objectives;
		this.rewards = quest.rewards;
		this.state = quest.state;
		this.questID = quest.questID;
		this.requirements = quest.requirements;
	}

	public String getQuestName() {
		return questName;
	}

	public QuestFirstNpc getFirstNPC() {
		return firstNPC;
	}

	public HashMap<QuestObjective, QuestObjectiveState> getObjectives() {
		return objectives;
	}

	public QuestRewards getRewards() {
		return rewards;
	}

	public QuestState getQuestState() {
		return state;
	}

	public Integer getQuestID() {
		return questID;
	}

	public QuestRequirements getRequirements() {
		return requirements;
	}

}