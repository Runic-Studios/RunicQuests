package com.runicrealms.plugin.runicquests.quests.objective;

import com.runicrealms.plugin.runicquests.quests.QuestItem;
import com.runicrealms.plugin.runicquests.quests.QuestObjectiveType;

import java.util.List;

/**
 * Represents the goal of casting a spell (or spells) to progress a quest
 */
public class QuestObjectiveCast extends QuestObjective {
    private List<String> spellNames;
    private Integer spellAmount;
    private Integer spellsCasted = 0; // Current amount

    public QuestObjectiveCast(
            List<String> spellNames,
            Integer spellAmount,
            List<QuestItem> questItems,
            String goalMessage,
            List<String> execute,
            Integer objectiveNumber,
            List<String> completedMessage,
            String goalLocation,
            boolean displayNextTitle) {
        super(objectiveNumber, QuestObjectiveType.CAST, completedMessage, questItems, goalMessage, execute, goalLocation, displayNextTitle);
        this.spellNames = spellNames;
        this.spellAmount = spellAmount;
    }

    @Override
    public QuestObjectiveCast clone() {
        return new QuestObjectiveCast
                (
                        this.spellNames,
                        this.spellAmount,
                        this.questItems,
                        this.goalMessage,
                        this.execute,
                        this.objectiveNumber,
                        this.completedMessage,
                        this.goalLocation,
                        this.displayNextTitle
                );
    }

    @Override
    public void resetObjective() {
        this.setSpellsCasted(0);
    }

    public Integer getSpellAmount() {
        return spellAmount;
    }

    public void setSpellAmount(Integer spellAmount) {
        this.spellAmount = spellAmount;
    }

    public List<String> getSpellNames() {
        return spellNames;
    }

    public void setSpellNames(List<String> spellNames) {
        this.spellNames = spellNames;
    }

    public Integer getSpellsCasted() {
        return spellsCasted;
    }

    public void setSpellsCasted(Integer spellsCasted) {
        this.spellsCasted = spellsCasted;
    }

}
