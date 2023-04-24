package com.runicrealms.runicquests.api;

import com.runicrealms.runicquests.model.QuestProfileData;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestRewards;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player completes a quest
 */
public class QuestCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final QuestProfileData profile;
    private final Quest quest;
    private final QuestObjective objective;

    /**
     * @param profile   data wrapper of player
     * @param quest     that was completed
     * @param objective that triggered the complete
     */
    public QuestCompleteEvent(QuestProfileData profile, Quest quest, QuestObjective objective) {
        this.quest = quest;
        this.profile = profile;
        this.objective = objective;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public QuestObjective getObjective() {
        return objective;
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
