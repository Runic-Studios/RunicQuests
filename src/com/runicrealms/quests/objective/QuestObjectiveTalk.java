package com.runicrealms.quests.objective;

import java.util.List;

import com.runicrealms.quests.QuestItem;
import com.runicrealms.quests.QuestNpc;
import com.runicrealms.quests.QuestObjectiveType;

public class QuestObjectiveTalk extends QuestObjective{
	
	private QuestNpc questNpc;
	
	public QuestObjectiveTalk(QuestNpc questNpc, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		super(objectiveNumber, QuestObjectiveType.TALK, completedMessage, goalMessage, execute);
		this.questNpc = questNpc;
	}
	
	public QuestObjectiveTalk(QuestNpc questNpc, List<QuestItem> questItems, List<String> goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		super(objectiveNumber, QuestObjectiveType.TALK, completedMessage, questItems, goalMessage, execute);
		this.questNpc = questNpc;
	}
	
	public QuestNpc getQuestNpc() {
		return this.questNpc;
	}
	
}
