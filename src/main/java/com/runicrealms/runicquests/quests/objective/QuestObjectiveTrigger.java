package com.runicrealms.runicquests.quests.objective;

import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.trigger.TriggerType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the trigger objective. Can be a single trigger or multiple
 */
public class QuestObjectiveTrigger extends QuestObjective {
    private final List<String> triggerIds;
    private final List<String> speech;
    private final TriggerType triggerType;
    private final Set<String> triggersEarned = new HashSet<>();

    public QuestObjectiveTrigger(
            List<String> triggerIds,
            List<String> speech,
            TriggerType triggerType,
            List<QuestItem> questItems,
            String goalMessage,
            List<String> execute,
            Integer objectiveNumber,
            List<String> completedMessage,
            String goalLocation,
            boolean displayNextTitle) {
        super(objectiveNumber, QuestObjectiveType.TRIGGER, completedMessage, questItems, goalMessage, execute, goalLocation, displayNextTitle);
        this.triggerIds = triggerIds;
        this.speech = speech;
        this.triggerType = triggerType;
    }

    @Override
    public QuestObjectiveTrigger clone() {
        return new QuestObjectiveTrigger
                (
                        this.triggerIds,
                        this.speech,
                        this.triggerType,
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

    }

    public List<String> getSpeech() {
        return this.speech;
    }

    public List<String> getTriggerIds() {
        return this.triggerIds;
    }

    public TriggerType getTriggerType() {
        return this.triggerType;
    }

    public Set<String> getTriggersEarned() {
        return triggersEarned;
    }

}
