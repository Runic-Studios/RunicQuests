package com.runicrealms.runicquests.quests.objective;

import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestNpc;
import com.runicrealms.runicquests.quests.QuestObjectiveType;

import java.util.List;

public class QuestObjectiveTalk extends QuestObjective {

    /*
     * Talk quest objective
     */

    private final QuestNpc questNpc;

    public QuestObjectiveTalk(QuestNpc questNpc, List<QuestItem> questItems, String goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage, String goalLocation, boolean displayNextTitle) {
        super(objectiveNumber, QuestObjectiveType.TALK, completedMessage, questItems, goalMessage, execute, goalLocation, displayNextTitle);
        this.questNpc = questNpc;
    }

    public QuestNpc getQuestNpc() {
        return this.questNpc;
    }

    @Override
    public QuestObjectiveTalk clone() {
        return new QuestObjectiveTalk(this.questNpc.clone(), this.questItems, this.goalMessage, this.execute, this.objectiveNumber, this.completedMessage, this.goalLocation, this.displayNextTitle);
    }

}
