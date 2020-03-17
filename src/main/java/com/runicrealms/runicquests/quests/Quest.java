package com.runicrealms.runicquests.quests;

import java.util.ArrayList;
import java.util.List;

import com.runicrealms.runicquests.player.QuestState;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;

public class Quest implements Cloneable {
	
	/*
	 * Contains all the quest values which are needed.
	 * All values except "QuestState state" can be parsed from config
	 */
	
	private String questName;
	private QuestFirstNpc firstNPC;
	private List<QuestObjective> objectives = new ArrayList<QuestObjective>();
	private QuestRewards rewards;
	private QuestState state;
	private Integer questID;
	private QuestRequirements requirements;
	private boolean sideQuest;
	private boolean repeatable;
	private Integer cooldown;
	
	public Quest(String questName, QuestFirstNpc firstNPC, ArrayList<QuestObjective> objectives, QuestRewards rewards, Integer questID, QuestRequirements requirements, boolean sideQuest, boolean repeatable, Integer cooldown) {
		this.questName = questName;
		this.firstNPC = firstNPC;
		this.objectives = objectives;
		this.rewards = rewards;
		this.state = new QuestState(false, false);
		this.questID = questID;
		this.requirements = requirements;
		this.sideQuest = sideQuest;
		this.repeatable = repeatable;
		this.cooldown = cooldown;
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
		this.cooldown = quest.cooldown;
	}

	public String getQuestName() {
		return questName;
	}

	public QuestFirstNpc getFirstNPC() {
		return firstNPC;
	}

	public List<QuestObjective> getObjectives() {
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
	
	public boolean hasCooldown() {
		return this.cooldown != null;
	}
	
	public Integer getCooldown() {
		return this.cooldown;
	}

	@Override
	public Quest clone() {
		ArrayList<QuestObjective> newObjectives = new ArrayList<QuestObjective>() ;
		for (QuestObjective objective : this.objectives) {
			if (objective instanceof QuestObjectiveTalk) {
				newObjectives.add(((QuestObjectiveTalk) objective).clone());
			} else {
				newObjectives.add(objective);
			}
		}
		return new Quest(this.questName, this.firstNPC.clone(), newObjectives, this.rewards, this.questID, this.requirements, this.sideQuest, this.repeatable, this.cooldown);
	}

}