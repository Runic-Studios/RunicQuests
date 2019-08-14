package com.runicrealms.quests;

import java.util.ArrayList;
import java.util.HashMap;

public class Quest {
	
	public String questName;
	public QuestFirstNpc firstNPC;
	public HashMap<QuestObjective, ObjectiveState> objectives;
	public QuestRewards rewards;
	public QuestState state;
	
	public Quest(String questName, QuestFirstNpc firstNPC, ArrayList<QuestObjective> objectives, QuestRewards rewards) {
		this.questName = questName;
		this.firstNPC = firstNPC;
		for (QuestObjective objective : objectives) {
			this.objectives.put(objective, new ObjectiveState());
		}
		this.rewards = rewards;
		this.state = new QuestState(false, false);
	}
	
	public Quest(Quest quest) {
		this.questName = quest.questName;
		this.firstNPC = quest.firstNPC;
		this.objectives = quest.objectives;
		this.rewards = quest.rewards;
		this.state = quest.state;
	}
	
}