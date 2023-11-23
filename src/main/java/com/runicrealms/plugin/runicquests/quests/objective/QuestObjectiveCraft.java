package com.runicrealms.plugin.runicquests.quests.objective;

import com.runicrealms.plugin.runicquests.quests.QuestItem;
import com.runicrealms.plugin.runicquests.quests.QuestObjectiveType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents the goal of crafting an item to progress a quest
 *
 * @author BoBoBalloon
 */
public class QuestObjectiveCraft extends QuestObjective {
    private final List<String> templateIds;
    private final int requiredToComplete;
    private int crafted;

    public QuestObjectiveCraft(
            @NotNull List<String> templateIds,
            int requiredToComplete,
            @Nullable List<QuestItem> questItems,
            @NotNull String goalMessage,
            @Nullable List<String> execute,
            @NotNull Integer objectiveNumber,
            @Nullable List<String> completedMessage,
            @NotNull String goalLocation,
            boolean displayNextTitle) {
        super(objectiveNumber, QuestObjectiveType.CRAFT, completedMessage, questItems, goalMessage, execute, goalLocation, displayNextTitle);
        this.templateIds = templateIds;
        this.requiredToComplete = requiredToComplete;
        this.crafted = 0;
    }

    @Override
    public QuestObjectiveCraft clone() {
        return new QuestObjectiveCraft
                (
                        this.templateIds,
                        this.requiredToComplete,
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
        this.crafted = 0;
    }

    @NotNull
    public List<String> getTemplateIds() {
        return this.templateIds;
    }

    public int getRequiredToComplete() {
        return this.requiredToComplete;
    }

    public int getCrafted() {
        return this.crafted;
    }

    public void setCrafted(int crafted) {
        this.crafted = crafted;
    }
}
