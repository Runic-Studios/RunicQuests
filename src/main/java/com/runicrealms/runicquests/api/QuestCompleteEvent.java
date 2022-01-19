package com.runicrealms.runicquests.api;

import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestRewards;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class QuestCompleteEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Quest quest;
    private final QuestProfile profile;

    public QuestCompleteEvent(Quest quest, QuestProfile profile) {
        this.quest = quest;
        this.profile = profile;
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

}
