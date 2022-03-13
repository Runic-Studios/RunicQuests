package com.runicrealms.runicquests.listeners;

import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JoinQuitListener implements Listener {

    public static void runJoinEvent(Player player, Integer characterSlot) {
        PlayerDataLoader.addPlayerQuestData(player.getUniqueId(), characterSlot, () -> Plugin.updatePlayerCachedLocations(player)); // Add a quest profile
    }

    public static void runQuitEvent(Player player) {
        Plugin.getQuestCooldowns().remove(player.getUniqueId()); // Remove the cooldown object
        QuestProfile questProfile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId()); // Get the quest profile
        for (Quest quest : questProfile.getQuests()) { // Loop through the quests
            for (QuestObjective objective : quest.getObjectives()) { // Loop through objectives
                if (objective.getObjectiveType() == QuestObjectiveType.TALK) { // Check for objective of type talk
                    /*
                     * This is a minor bug fix which prevents minor issues with players
                     * talking to NPCs, then logging out
                     */
                    Plugin.getNpcTaskQueues().remove(((QuestObjectiveTalk) objective).getQuestNpc().getId());
                }
            }
        }
        PlayerDataLoader.getAllPlayerData().remove(player.getUniqueId());
        Plugin.getCachedLocations().remove(player);
    }

    public static void refreshPlayerData(Player player) {
        Plugin.getQuestCooldowns().remove(player.getUniqueId());
        QuestProfile questProfile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId());
        for (Quest quest : questProfile.getQuests()) {
            for (QuestObjective objective : quest.getObjectives()) {
                if (objective.getObjectiveType() == QuestObjectiveType.TALK) {
                    Plugin.getNpcTaskQueues().remove(((QuestObjectiveTalk) objective).getQuestNpc().getId());
                }
            }
        }
        Plugin.getCachedLocations().remove(player);
        Plugin.updatePlayerCachedLocations(player);
    }

    @EventHandler
    public void onPlayerJoin(CharacterLoadEvent event) {
        runJoinEvent(event.getPlayer(), event.getSlot());
    }

    @EventHandler
    public void onPlayerQuit(CharacterQuitEvent event) {
        runQuitEvent(event.getPlayer());
    }
}