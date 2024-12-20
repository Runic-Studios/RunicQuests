package com.runicrealms.plugin.runicquests.quests.objective;

import com.runicrealms.plugin.runicquests.quests.QuestItem;
import com.runicrealms.plugin.runicquests.quests.QuestNpc;
import com.runicrealms.plugin.runicquests.quests.QuestObjectiveType;

import java.util.List;

public class QuestObjectiveTalk extends QuestObjective {
    private final QuestNpc questNpc;

    public QuestObjectiveTalk(QuestNpc questNpc, List<QuestItem> questItems, String goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage, String goalLocation, boolean displayNextTitle) {
        super(objectiveNumber, QuestObjectiveType.TALK, completedMessage, questItems, goalMessage, execute, goalLocation, displayNextTitle);
        this.questNpc = questNpc;
    }

    @Override
    public QuestObjectiveTalk clone() {
        return new QuestObjectiveTalk(this.questNpc.clone(), this.questItems, this.goalMessage, this.execute, this.objectiveNumber, this.completedMessage, this.goalLocation, this.displayNextTitle);
    }

    @Override
    public void resetObjective() {

    }

    public QuestNpc getQuestNpc() {
        return this.questNpc;
    }

}
