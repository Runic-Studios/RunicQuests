package com.runicrealms.plugin.runicquests.listeners;

import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicquests.api.QuestCompleteEvent;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjective;
import com.runicrealms.plugin.runicquests.task.TaskQueue;
import com.runicrealms.plugin.runicquests.util.RunicCoreHook;
import com.runicrealms.plugin.runicquests.util.SpeechParser;
import com.runicrealms.plugin.runicquests.quests.Quest;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestCompleteListener implements Listener {

    /**
     * @param player    who completed the quest
     * @param quest     that was completed
     * @param objective that triggered the complete
     */
    private void handleQuestComplete(Player player, Quest quest, QuestObjective objective) {
        quest.getQuestState().setCompleted(true);
        if (objective.hasCompletedMessage()) { // If we have a completed message, create a task queue and add completed message, and rewards to queue
            List<Runnable> runnableList = new ArrayList<>();
            SpeechParser speechParser = new SpeechParser(player);
            for (String message : objective.getCompletedMessage()) {
                runnableList.add(() -> {
                    speechParser.updateParsedMessage(message);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', speechParser.getParsedMessage()));
                    speechParser.executeCommands();
                });
            }
            TaskQueue queue = new TaskQueue(runnableList);
            queue.addTasks(() -> {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1); // Play sound
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
                if (quest.getRewards().getQuestPointsReward() != 0)
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() == 1 ? "" : "s")));
                if (quest.getRewards().getMoneyReward() != 0)
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() == 1 ? "" : "s")));
                if (quest.getRewards().getExperienceReward() != 0)
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience Point" + (quest.getRewards().getExperienceReward() == 1 ? "" : "s")));
                if (objective.shouldDisplayNextObjectiveTitle())
                    player.sendTitle(ChatColor.DARK_GREEN + "Quest Complete!", ChatColor.GREEN + quest.getQuestName(), 10, 80, 10); // Send a goal message title
            });
            queue.startTasks();
        } else { // If we don't have a completed message, display rewards
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1); // Play sound
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
            if (quest.getRewards().getQuestPointsReward() != 0)
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() == 1 ? "" : "s")));
            if (quest.getRewards().getMoneyReward() != 0)
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() == 1 ? "" : "s")));
            if (quest.getRewards().getExperienceReward() != 0)
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience Point" + (quest.getRewards().getExperienceReward() == 1 ? "" : "s")));
            if (objective.shouldDisplayNextObjectiveTitle())
                player.sendTitle(ChatColor.DARK_GREEN + "Quest Complete!", ChatColor.GREEN + quest.getQuestName(), 10, 80, 10); // Send a goal message title
        }
        if (quest.getRewards().hasExecute()) { // Execute quest commands
            quest.getRewards().executeCommand(player.getName());
        }
        // give items
        for (Map.Entry<String, Integer> entry : quest.getRewards().getItems().entrySet()) {
            RunicItemsAPI.addItem(player.getInventory(), RunicItemsAPI.generateItemFromTemplate(entry.getKey(), entry.getValue()).generateItem());
        }
        RunicCoreHook.giveRewards(player, quest.getRewards()); // Give rewards
    }

    /**
     * Handles logic for when a quest is completed
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuestComplete(QuestCompleteEvent event) {
        handleQuestComplete(event.getPlayer(), event.getQuest(), event.getObjective());
    }
}
