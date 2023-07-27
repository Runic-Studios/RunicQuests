package com.runicrealms.plugin.runicquests.quests.objective;

import com.runicrealms.plugin.runicquests.api.QuestCompleteEvent;
import com.runicrealms.plugin.runicquests.api.QuestCompleteObjectiveEvent;
import com.runicrealms.plugin.runicquests.model.QuestProfileData;
import com.runicrealms.plugin.runicquests.task.TaskQueue;
import com.runicrealms.plugin.runicquests.util.SpeechParser;
import com.runicrealms.plugin.runicquests.RunicQuests;
import com.runicrealms.plugin.runicquests.quests.FirstNpcState;
import com.runicrealms.plugin.runicquests.quests.Quest;
import com.runicrealms.plugin.runicquests.quests.QuestItem;
import com.runicrealms.plugin.runicquests.quests.QuestObjectiveType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public interface QuestObjectiveHandler {

    /**
     * Checks if the given objective for quest object is the current objective of the player
     *
     * @param quest     object of given player
     * @param objective to check
     * @return true if all previous objectives have been completed
     */
    default boolean isCurrentObjective(Quest quest, QuestObjective objective) {
        if (objective.getObjectiveNumber() == 1)
            return true;
        QuestObjective previous = QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 1);
        if (previous == null) {
            return true;
        }
        return previous.isCompleted();
    }

    /**
     * Checks if the quest is "active".
     * True if the quest is non-repeatable, has started, and has not finished
     * True if the quest is repeatable and has started
     *
     * @param quest to check
     * @return true if active
     */
    default boolean isQuestActive(Quest quest) {
        /*
                (!quest.getQuestState().isCompleted() && quest.getQuestState().hasStarted())
                || (quest.isRepeatable() && quest.getQuestState().isCompleted() && quest.getQuestState().hasStarted())
         */
        boolean hasStarted = quest.getQuestState().hasStarted();
        boolean isComplete = quest.getQuestState().isCompleted();
        boolean isRepeatable = quest.isRepeatable();
        if (hasStarted && !isComplete) {
            return true;
        }
        return isRepeatable && hasStarted;
    }

    /**
     * Checks if the given objective is 'valid', meaning it is the current objective in an active quest
     * and of the correct type
     *
     * @param quest         that will be progressed
     * @param objective     to check
     * @param objectiveType the type of objective 'slay, break, etc.'
     * @return true if it is valid
     */
    default boolean isValidObjective(Quest quest, QuestObjective objective, QuestObjectiveType objectiveType) {
        if (objective.isCompleted()) return false;
        if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) return false;
        if (!isCurrentObjective(quest, objective)) return false;
        return objective.getObjectiveType() == objectiveType;
    }

    /**
     * @param player      to progress
     * @param profileData their quest data wrapper
     * @param quest       that is being progressed
     * @param objective   that triggered progress
     */
    default void progressObjective(Player player, QuestProfileData profileData, Quest quest, QuestObjective objective) {
        QuestObjective nextObjective = QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1);
        String goalMessage;
        if (nextObjective != null) {
            goalMessage = ChatColor.translateAlternateColorCodes('&', nextObjective.getGoalMessage());
        } else {
            goalMessage = "";
        }
        if (objective.hasCompletedMessage()) { // If objective has a completed message, create a task queue and add message + new objective message to it
            List<Runnable> runnableList = new ArrayList<>();
            SpeechParser speechParser = new SpeechParser(player);
            for (String message : objective.getCompletedMessage()) {
                runnableList.add(() -> {
                    speechParser.updateParsedMessage(ChatColor.translateAlternateColorCodes('&', (message)));
                    player.sendMessage(speechParser.getParsedMessage());
                    speechParser.executeCommands();
                });
            }
            runnableList.add(() -> {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
                if (objective.shouldDisplayNextObjectiveTitle())
                    player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 80, 10);  // Display a title on the screen
                RunicQuests.getLocationManager().updatePlayerCachedLocations(player);
                Bukkit.getScheduler().runTask(RunicQuests.getInstance(),
                        () -> Bukkit.getPluginManager().callEvent(new QuestCompleteObjectiveEvent(quest, profileData, objective)));
            });
            TaskQueue queue = new TaskQueue(runnableList);
            queue.startTasks();
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
            if (objective.shouldDisplayNextObjectiveTitle())
                player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 80, 10);  // Display a title on the screen
            RunicQuests.getLocationManager().updatePlayerCachedLocations(player);
            Bukkit.getScheduler().runTask(RunicQuests.getInstance(),
                    () -> Bukkit.getPluginManager().callEvent(new QuestCompleteObjectiveEvent(quest, profileData, objective)));
        }
    }

    /**
     * Progresses a quest to the next objective, and completes the quest if there are no more objectives
     * Must be run SYNC to execute commands
     *
     * @param player      to progress
     * @param profileData their quest data wrapper
     * @param quest       that is being progressed
     * @param objective   that triggered progress
     */
    default void progressQuest(Player player, QuestProfileData profileData, Quest quest, QuestObjective objective) {
        objective.setCompleted(true);
        if (objective.hasExecute()) { // Executes the objective commands
            objective.executeCommand(player.getName());
        }
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f); // Play a sound
        QuestObjective finalObjective = QuestObjective.getLastObjective(quest.getObjectives());
        if (finalObjective != null && !objective.getObjectiveNumber().equals(finalObjective.getObjectiveNumber())) { // If we have not finished the quest
            progressObjective(player, profileData, quest, objective);
        } else { // Quest complete, call event SYNC
            Bukkit.getServer().getPluginManager().callEvent(new QuestCompleteEvent(profileData, quest, objective));
        }
    }

    /**
     * @param player    to progress
     * @param objective that triggered progress
     * @return true if no item requirement or item requirement exists and is met
     */
    default boolean questItemRequirementMet(Player player, QuestObjective objective) {
        if (objective.requiresQuestItem()) { // Check for quest item
            if (RunicQuests.hasQuestItems(objective, player)) {
                for (QuestItem questItem : objective.getQuestItems()) {
                    RunicQuests.removeItem(player, questItem.getItemName(), questItem.getItemType(), questItem.getAmount());
                }
                player.updateInventory();
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
}
