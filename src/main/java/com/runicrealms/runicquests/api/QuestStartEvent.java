package com.runicrealms.runicquests.api;

import com.runicrealms.runicquests.model.QuestProfileData;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestFirstNpc;
import com.runicrealms.runicquests.quests.QuestRewards;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player begins a quest
 */
public class QuestStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Quest quest;
    private final QuestProfileData profile;
    private final QuestFirstNpc firstNpc;

    public QuestStartEvent(Quest quest, QuestProfileData profile, QuestFirstNpc firstNpc) {
        this.quest = quest;
        this.profile = profile;
        this.firstNpc = firstNpc;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
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

    public QuestFirstNpc getQuestFirstNpc() {
        return this.firstNpc;
    }

    public QuestProfileData getQuestProfile() {
        return this.profile;
    }

    public QuestRewards getRewards() {
        return this.quest.getRewards();
    }

}
