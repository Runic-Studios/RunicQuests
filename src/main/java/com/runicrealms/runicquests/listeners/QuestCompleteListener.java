package com.runicrealms.runicquests.listeners;

import com.runicrealms.runicquests.api.QuestCompleteEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * This is just insurance.
 * - Sky
 */
public class QuestCompleteListener implements Listener {

    @EventHandler
    public void onQuestComplete(QuestCompleteEvent e) {
        e.getQuest().getQuestState().setCompleted(true);
    }
}
