package com.runicrealms.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.runicrealms.player.QuestObjectiveState;
import com.runicrealms.player.QuestState;

public class Quest {
	
	private String questName;
	private QuestFirstNpc firstNPC;
	private HashMap<QuestObjective, QuestObjectiveState> objectives = new HashMap<QuestObjective, QuestObjectiveState>();
	private QuestRewards rewards;
	private QuestState state;
	private Integer questID;
	private QuestRequirements requirements;
	private boolean sideQuest;
	private boolean repeatable;
	private List<String> completionSpeech;
	private Boolean useLastNpcNameForCompletionSpeech;
	
	public Quest(String questName, QuestFirstNpc firstNPC, ArrayList<QuestObjective> objectives, QuestRewards rewards, Integer questID, QuestRequirements requirements, boolean sideQuest, boolean repeatable, List<String> completionSpeech, Boolean useLastNpcNameForCompletionSpeech) {
		this.questName = questName;
		this.firstNPC = firstNPC;
		for (QuestObjective objective : objectives) {
			this.objectives.put(objective, new QuestObjectiveState());
		}
		this.rewards = rewards;
		this.state = new QuestState(false, false);
		this.questID = questID;
		this.requirements = requirements;
		this.sideQuest = sideQuest;
		this.repeatable = repeatable;
		this.completionSpeech = completionSpeech;
		this.useLastNpcNameForCompletionSpeech = useLastNpcNameForCompletionSpeech;
	}
	
	public Quest(Quest quest) {
		this.questName = quest.questName;
		this.firstNPC = quest.firstNPC;
		this.objectives = quest.objectives;
		this.rewards = quest.rewards;
		this.state = quest.state;
		this.questID = quest.questID;
		this.requirements = quest.requirements;
		this.sideQuest = quest.sideQuest;
		this.repeatable = quest.repeatable;
		this.completionSpeech = quest.completionSpeech;
		this.useLastNpcNameForCompletionSpeech = quest.useLastNpcNameForCompletionSpeech;
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
	
	public boolean isSideQuest() {
		return sideQuest;
	}

	public QuestRequirements getRequirements() {
		return requirements;
	}
	
	public boolean isRepeatable() {
		return repeatable;
	}
	
	public boolean hasCompletionSpeech() {
		return completionSpeech != null;
	}
	
	public List<String> getCompletionSpeech() {
		return completionSpeech;
	}
	
	public Boolean useLastNpcNameForCompletionSpeech() {
		return useLastNpcNameForCompletionSpeech;
	}

}