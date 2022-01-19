package com.runicrealms.runicquests.event;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.api.QuestCompleteEvent;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.FirstNpcState;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.task.TaskQueue;
import com.runicrealms.runicquests.util.RunicCoreHook;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventPlayerLocation implements Listener {

    public static void playerCompleteLocationObjective(Player player, Integer questID) {
        QuestProfile questProfile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId());
        Map<UUID, Map<Integer, Long>> questCooldowns = Plugin.getQuestCooldowns();
        Quest questFound = null;
        for (Quest otherQuest : questProfile.getQuests()) {
            if (otherQuest.getQuestID() == questID) {
                questFound = otherQuest;
            }
        }
        final Quest quest = new Quest(questFound);
        if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) {
            return;
        }
        if (quest.getQuestState().isCompleted()) {
            return;
        }
        for (QuestObjective objective : quest.getObjectives()) {
            if (objective.isCompleted()) { // Check that the objective has not been completed
                continue;
            }
            if (objective.getObjectiveNumber() != 1) { // Check that the previous objective has been completed
                if (!QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 1).isCompleted()) {
                    continue;
                }
            }
            if (objective.getObjectiveType() != QuestObjectiveType.LOCATION) { // Check that the objective is of type location
                continue;
            }
            if (objective.requiresQuestItem()) { // Check for a quest item
                if (Plugin.hasQuestItems(objective, player)) {
                    for (QuestItem questItem : objective.getQuestItems()) {
                        Plugin.removeItem(player, questItem.getItemName(), questItem.getItemType(), questItem.getAmount());
                    }
                    player.updateInventory();
                } else {
                    continue;
                }
            }
            objective.setCompleted(true);
            questProfile.save();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 0); // Play sound
            if (objective.hasExecute()) { // Execute objective commands
                objective.executeCommand(player.getName());
            }
            if (!objective.getObjectiveNumber().equals(QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber())) { // Check that we haven't completed the quest
                String goalMessage = ChatColor.translateAlternateColorCodes('&', QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage()); // Get the goal message
                if (objective.hasCompletedMessage()) { // Check for a completed message
                    List<Runnable> runnables = new ArrayList<Runnable>();
                    for (String message : objective.getCompletedMessage()) { // Create a task queue with the completed message
                        runnables.add(() -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', Plugin.parseMessage(message, player.getName()))));
                    }
                    // Add the new objective message to the task queue
                    runnables.add(() -> {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + goalMessage));
                        if (objective.shouldDisplayNextObjectiveTitle())
                            player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 80, 10); // Send a goal message title
                        Plugin.updatePlayerCachedLocations(player);
                    });
                    TaskQueue queue = new TaskQueue(runnables);
                    queue.startTasks();
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + goalMessage));
                    if (objective.shouldDisplayNextObjectiveTitle())
                        player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 80, 10); // Send a goal message title
                    Plugin.updatePlayerCachedLocations(player);
                }
            } else { // If we have finished the quest
                quest.getQuestState().setCompleted(true);
                questProfile.save(questProfile.getQuestPoints() + quest.getRewards().getQuestPointsReward());
                if (objective.hasCompletedMessage()) { // If we have a completed message
                    List<Runnable> runnables = new ArrayList<Runnable>();
                    for (String message : objective.getCompletedMessage()) { // Create a task queue with the completed message
                        runnables.add(() -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', Plugin.parseMessage(message, player.getName()))));
                    }
                    TaskQueue queue = new TaskQueue(runnables);
                    // Add the quest rewards to the task queue
                    queue.addTasks(() -> {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1); // Play sound
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() == 1 ? "" : "s")));
                        if (quest.getRewards().getMoneyReward() != 0)
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() == 1 ? "" : "s")));
                        if (quest.getRewards().getExperienceReward() != 0)
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience Point" + (quest.getRewards().getExperienceReward() == 1 ? "" : "s")));
                    });
                    queue.startTasks();
                } else { // If we don't have a completed message, display the rewards
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1); // Play sound
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() == 1 ? "" : "s")));
                    if (quest.getRewards().getMoneyReward() != 0)
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() == 1 ? "" : "s")));
                    if (quest.getRewards().getExperienceReward() != 0)
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience Point" + (quest.getRewards().getExperienceReward() == 1 ? "" : "s")));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + "Quest Complete!"));
                    if (objective.shouldDisplayNextObjectiveTitle())
                        player.sendTitle(ChatColor.GOLD + "Quest Complete!", ChatColor.YELLOW + quest.getQuestName(), 10, 80, 10); // Send a goal message title
                }
                if (quest.getRewards().hasExecute()) { // Execute the rewards commands
                    quest.getRewards().executeCommand(player.getName());
                }
                RunicCoreHook.giveRewards(player, quest.getRewards()); // Give the rewards
                Bukkit.getServer().getPluginManager().callEvent(new QuestCompleteEvent(quest, questProfile)); // Fire the quest completed event
            }
            break;
        }
    }

}
