package com.runicrealms.plugin.runicquests.quests.objective;

import com.runicrealms.plugin.runicquests.quests.QuestItem;
import com.runicrealms.plugin.runicquests.quests.QuestObjectiveType;

import java.util.List;

/**
 * Objective of type gather, triggered when a player gathers a material (must actually obtain the resource themselves)
 */
public class QuestObjectiveGather extends QuestObjective {
    private final List<String> templateIds; // From RunicItems
    private final Integer gatherAmount;
    private Integer resourcesGathered = 0; // Current amount

    public QuestObjectiveGather(
            List<String> templateIds,
            Integer gatherAmount,
            List<QuestItem> questItems,
            String goalMessage,
            List<String> execute,
            Integer objectiveNumber,
            List<String> completedMessage,
            String goalLocation,
            boolean displayNextTitle) {
        super(objectiveNumber, QuestObjectiveType.GATHER, completedMessage, questItems, goalMessage, execute, goalLocation, displayNextTitle);
        this.templateIds = templateIds;
        this.gatherAmount = gatherAmount;
    }

    @Override
    public QuestObjectiveGather clone() {
        return new QuestObjectiveGather
                (
                        this.templateIds,
                        this.gatherAmount,
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
        this.setResourcesGathered(0);
    }

    public Integer getGatherAmount() {
        return this.gatherAmount;
    }

    public Integer getResourcesGathered() {
        return this.resourcesGathered;
    }

    public void setResourcesGathered(Integer amount) {
        this.resourcesGathered = amount;
    }

    public List<String> getTemplateIds() {
        return this.templateIds;
    }

    public boolean hasGatherAmount() {
        return this.gatherAmount != null;
    }


}
