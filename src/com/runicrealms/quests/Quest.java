package com.runicrealms.quests;

import java.util.List;

public class Quest {
	
	public String questName;
	public QuestNpc firstNPC;
	public List<QuestObjective> objectives;
	public QuestRewards rewards;
	
	public Quest(String questName, QuestNpc firstNPC, List<QuestObjective> objectives, QuestRewards rewards) {
		this.questName = questName;
		this.firstNPC = firstNPC;
		this.objectives = objectives;
		this.rewards = rewards;
	}
	
}