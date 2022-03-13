package com.runicrealms.runicquests.api;

import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestRewards;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

// WARNING: does not fire on completion of last objective, see quest complete event
// Also does not fire on quest start, see quest start event
public class QuestCompleteObjectiveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Quest quest;
    private final QuestProfile profile;
    private final QuestObjective objectiveCompleted;

    public QuestCompleteObjectiveEvent(Quest quest, QuestProfile profile, QuestObjective objectiveCompleted) {
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

    public Quest getQuest() {
        return this.quest;
    }

    public QuestProfile getQuestProfile() {
        return this.profile;
    }

    public Player getPlayer() {
        try {
            return Bukkit.getPlayer(UUID.fromString(this.profile.getUuid()));
        } catch (Exception exception) {
            return null;
        }
    }

    public QuestRewards getRewards() {
        return this.quest.getRewards();
    }

    public QuestObjective getObjectiveCompleted() {
        return this.objectiveCompleted;
    }

}
