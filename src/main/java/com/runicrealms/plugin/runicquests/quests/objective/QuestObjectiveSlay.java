package com.runicrealms.plugin.runicquests.quests.objective;

import com.runicrealms.plugin.runicquests.quests.QuestItem;
import com.runicrealms.plugin.runicquests.quests.QuestObjectiveType;

import java.util.List;

public class QuestObjectiveSlay extends QuestObjective {
    private final List<String> mobNames;
    private final Integer mobAmount;
    private Integer mobsKilled = 0;

    public QuestObjectiveSlay(List<String> mobNames, Integer mobAmount, List<QuestItem> questItems, String goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage, String goalLocation, boolean displayNextTitle) {
        super(objectiveNumber, QuestObjectiveType.SLAY, completedMessage, questItems, goalMessage, execute, goalLocation, displayNextTitle);
        this.mobNames = mobNames;
        this.mobAmount = mobAmount;
    }

    @Override
    public QuestObjectiveSlay clone() {
        return new QuestObjectiveSlay(this.mobNames, this.mobAmount, this.questItems, this.goalMessage, this.execute, this.objectiveNumber, this.completedMessage, this.goalLocation, this.displayNextTitle);
    }

    @Override
    public void resetObjective() {
        this.setMobsKilled(0);
    }

    public Integer getMobAmount() {
        return this.mobAmount;
    }

    public List<String> getMobNames() {
        return this.mobNames;
    }

    public Integer getMobsKilled() {
        return this.mobsKilled;
    }

    public void setMobsKilled(Integer mobsKilled) {
        this.mobsKilled = mobsKilled;
    }

}
