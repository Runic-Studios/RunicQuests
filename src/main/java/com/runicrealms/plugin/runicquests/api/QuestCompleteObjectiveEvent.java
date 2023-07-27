package com.runicrealms.plugin.runicquests.api;

import com.runicrealms.plugin.runicquests.model.QuestProfileData;
import com.runicrealms.plugin.runicquests.quests.Quest;
import com.runicrealms.plugin.runicquests.quests.QuestRewards;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjective;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the player progresses an objective of a quest that is neither the start, nor the end
 * WARNING: this event does not fire on completion of last objective, see quest complete event
 * Also does not fire on quest start, see quest start event
 */
public class QuestCompleteObjectiveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Quest quest;
    private final QuestProfileData profile;
    private final QuestObjective objectiveCompleted;

    public QuestCompleteObjectiveEvent(Quest quest, QuestProfileData profile, QuestObjective objectiveCompleted) {
        this.quest = quest;
        this.profile = profile;
        this.objectiveCompleted = objectiveCompleted;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public QuestObjective getObjectiveCompleted() {
        return this.objectiveCompleted;
    }

    public Player getPlayer() {
        try {
            return Bukkit.getPlayer(this.profile.getUuid());
        } catch (Exception exception) {
            return null;
        }
    }

    public Quest getQuest() {
        return this.quest;
    }

    public QuestProfileData getQuestProfile() {
        return this.profile;
    }

    public QuestRewards getRewards() {
        return this.quest.getRewards();
    }

}
