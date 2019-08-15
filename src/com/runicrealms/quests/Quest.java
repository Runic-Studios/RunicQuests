package com.runicrealms.quests;

import java.util.ArrayList;
import java.util.HashMap;

import com.runicrealms.player.QuestObjectiveState;
import com.runicrealms.player.QuestState;

public class Quest {
	
	public String questName;
	public QuestFirstNpc firstNPC;
	public HashMap<QuestObjective, QuestObjectiveState> objectives;
	public QuestRewards rewards;
	public QuestState state;
	public Integer questID;
	
	public Quest(String questName, QuestFirstNpc firstNPC, ArrayList<QuestObjective> objectives, QuestRewards rewards, Integer questID) {
		this.questName = questName;
		this.firstNPC = firstNPC;
		for (QuestObjective objective : objectives) {
			this.objectives.put(objective, new QuestObjectiveState());
		}
		this.rewards = rewards;
		this.state = new QuestState(false, false);
		this.questID = questID;
	}
	
	public Quest(Quest quest) {
		this.questName = quest.questName;
		this.firstNPC = quest.firstNPC;
		this.objectives = quest.objectives;
		this.rewards = quest.rewards;
		this.state = quest.state;
		this.questID = quest.questID;
	}
	
}