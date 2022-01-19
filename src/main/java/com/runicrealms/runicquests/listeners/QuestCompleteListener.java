package com.runicrealms.runicquests.listeners;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.api.QuestCompleteEvent;
import com.runicrealms.runicquests.quests.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;

public class QuestCompleteListener implements Listener {

    /**
     * Manage repeatable quest cooldowns
     */
    @EventHandler
    public void onQuestComplete(QuestCompleteEvent e) {
        if (!e.getQuest().isRepeatable()) return;
        Player player = e.getPlayer();
        Quest quest = e.getQuest();
        Map<UUID, Map<Integer, Long>> questCooldowns = Plugin.getQuestCooldowns();
        questCooldowns.get(player.getUniqueId()).put(quest.getQuestID(), System.currentTimeMillis() + quest.getCooldown() * 1000);
    }
}
