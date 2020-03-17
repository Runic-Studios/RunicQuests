package com.runicrealms.runicquests.quests.objective;

import java.util.List;

import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestNpc;
import com.runicrealms.runicquests.quests.QuestObjectiveType;

public class QuestObjectiveTalk extends QuestObjective implements Cloneable {
	
	/*
	 * Talk quest objective
	 */
	
	private QuestNpc questNpc;
	
	public QuestObjectiveTalk(QuestNpc questNpc, List<QuestItem> questItems, String goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		super(objectiveNumber, QuestObjectiveType.TALK, completedMessage, questItems, goalMessage, execute);
		this.questNpc = questNpc;
	}
	
	public QuestNpc getQuestNpc() {
		return this.questNpc;
	}

	@Override
	public QuestObjectiveTalk clone() {
		return new QuestObjectiveTalk(this.questNpc.clone(), this.questItems, this.goalMessage, this.execute, this.objectiveNumber, this.completedMessage);
	}
	
}
