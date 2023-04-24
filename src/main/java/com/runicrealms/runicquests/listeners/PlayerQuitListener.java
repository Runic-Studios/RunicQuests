package com.runicrealms.runicquests.listeners;

import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.model.QuestProfileData;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onCharacterQuit(CharacterQuitEvent event) {
        Player player = event.getPlayer();
        RunicQuests.getQuestCooldowns().remove(player.getUniqueId()); // Remove the cooldown object
        QuestProfileData questProfileData = RunicQuests.getAPI().getQuestProfile(player.getUniqueId()); // Get the quest profile
        for (Quest quest : questProfileData.getQuestsMap().get(event.getSlot())) { // Loop through the quests
            for (QuestObjective objective : quest.getObjectives()) { // Loop through objectives
                if (objective.getObjectiveType() == QuestObjectiveType.TALK) { // Check for objective of type talk
                    /*
                     * This is a minor bug fix which prevents minor issues with players
                     * talking to NPCs, then logging out
                     */
                    RunicQuests.getNpcTaskQueues().remove(((QuestObjectiveTalk) objective).getQuestNpc().getId());
                }
            }
        }
        RunicQuests.getLocationManager().getCachedLocations().remove(player);
    }
}

