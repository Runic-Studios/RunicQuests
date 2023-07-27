package com.runicrealms.plugin.runicquests.listeners;

import com.runicrealms.plugin.runicquests.api.QuestCompleteEvent;
import com.runicrealms.plugin.runicquests.api.QuestStartEvent;
import com.runicrealms.plugin.runicquests.RunicQuests;
import com.runicrealms.plugin.runicquests.quests.FirstNpcState;
import com.runicrealms.plugin.runicquests.quests.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles logic for repeatable quests
 */
public class RepeatableQuestListener implements Listener {

    /**
     * Places the repeatable quest on cooldown
     *
     * @param player who completed the quest
     * @param quest  that was completed
     */
    private void handleRepeatableQuest(Player player, Quest quest) {
        Map<UUID, Map<Integer, Date>> questCooldowns = RunicQuests.getQuestCooldowns();
        questCooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        questCooldowns.get(player.getUniqueId()).put(quest.getQuestID(), new Date()); // Record when quest was finished
        quest.getQuestState().setStarted(false);
        quest.getFirstNPC().setState(FirstNpcState.NEUTRAL);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuestComplete(QuestCompleteEvent event) {
        if (!event.getQuest().isRepeatable()) return;
        handleRepeatableQuest(event.getPlayer(), event.getQuest());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuestStart(QuestStartEvent event) {
        if (!event.getQuest().isRepeatable()) return;
        event.getQuest().getQuestState().setCompleted(false);
    }
}
