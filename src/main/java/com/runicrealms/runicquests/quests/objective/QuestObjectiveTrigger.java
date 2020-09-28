package com.runicrealms.runicquests.quests.objective;

import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;

import java.util.List;

public class QuestObjectiveTrigger extends QuestObjective {

    /*
     * Trigger quest objective
     */

    private String triggerId;
    private List<String> speech;

    public QuestObjectiveTrigger(String triggerId, List<String> speech, List<QuestItem> questItems, String goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage, String goalLocation) {
        super(objectiveNumber, QuestObjectiveType.TRIGGER, completedMessage, questItems, goalMessage, execute, goalLocation);
        this.triggerId = triggerId;
        this.speech = speech;
    }

    public String getTriggerId() {
        return this.triggerId;
    }

    public List<String> getSpeech() {
        return this.speech;
    }

    @Override
    public QuestObjectiveTrigger clone() {
        return new QuestObjectiveTrigger(this.triggerId, this.speech, this.questItems, this.goalMessage, this.execute, this.objectiveNumber, this.completedMessage, this.goalLocation);
    }

}
