package com.runicrealms.runicquests.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterApi;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.api.QuestCompleteEvent;
import com.runicrealms.runicquests.api.QuestCompleteObjectiveEvent;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.FirstNpcState;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveSlay;
import com.runicrealms.runicquests.task.TaskQueue;
import com.runicrealms.runicquests.util.RunicCoreHook;
import com.runicrealms.runicquests.util.SpeechParser;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class MythicMobDeathListener implements Listener {

    private static final int KILL_COUNT_DIST_SQUARED = 250;
    private static final String PREFIX = "&6[Quest] »";

    public static void runMythicMobsKill(Player player, MythicMob mythicMob) {
        QuestProfile questProfile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId()); // Get player questing profile
        int characterSlot = CharacterApi.getCurrentCharacterSlot(player);
        for (Quest quest : questProfile.getQuests()) { // Loop through quest to find a matching objective to the mob killed
            if ((!quest.getQuestState().isCompleted() && quest.getQuestState().hasStarted())
                    || (quest.isRepeatable() && quest.getQuestState().isCompleted() && quest.getQuestState().hasStarted())) { // Checks if the quest is "active"
                for (QuestObjective objective : quest.getObjectives()) { // Loops through the objectives to find a match
                    if (objective.isCompleted()) { // Checks to see that the objective is not completed
                        continue;
                    }
                    if (objective.getObjectiveNumber() != 1) { // Checks that the previous objective is completed (so this objective is the current one)
                        if (!QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 1).isCompleted()) {
                            continue;
                        }
                    }
                    if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) { // Check that the player has accepted the quest
                        continue;
                    }
                    if (objective.getObjectiveType() == QuestObjectiveType.SLAY) { // Checks that the objective is of type slay
                        QuestObjectiveSlay slayObjective = (QuestObjectiveSlay) objective;
                        for (String mob : slayObjective.getMobNames()) { // Checks that the mob in the objective has the correct name
                            if (mythicMob.getInternalName().equalsIgnoreCase(mob)) {
                                slayObjective.setMobsKilled(slayObjective.getMobsKilled() + 1); // Add to the slayed mobs
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " " + mythicMob.getDisplayName() + " &6» &7[&a" + slayObjective.getMobsKilled() + "&7/" + slayObjective.getMobAmount() + "]"));
                                if (slayObjective.getMobsKilled() == slayObjective.getMobAmount()) { // Check if player has killed required amount
                                    if (objective.requiresQuestItem()) { // Check for quest item
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
                                    if (objective.hasExecute()) { // Executes the objective commands
                                        objective.executeCommand(player.getName());
                                    }
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 0); // Play a sound
                                    if (!objective.getObjectiveNumber().equals(QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber())) { // Check to see if we have not finished the quest
                                        String goalMessage = ChatColor.translateAlternateColorCodes('&', QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage()); // Get the goal message
                                        if (objective.hasCompletedMessage()) { // If objective has a completed message, create a task queue and add message + new objective message to it
                                            List<Runnable> runnables = new ArrayList<>();
                                            SpeechParser speechParser = new SpeechParser(player);
                                            for (String message : objective.getCompletedMessage()) {
                                                runnables.add(() -> {
                                                    speechParser.updateParsedMessage(ChatColor.translateAlternateColorCodes('&', (message)));
                                                    player.sendMessage(speechParser.getParsedMessage());
                                                    speechParser.executeCommands();
                                                });
                                            }
                                            runnables.add(() -> {
                                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
                                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
                                                if (objective.shouldDisplayNextObjectiveTitle())
                                                    player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 80, 10);  // Display a title on the screen
                                                Plugin.updatePlayerCachedLocations(player);
                                                Bukkit.getPluginManager().callEvent(new QuestCompleteObjectiveEvent(quest, questProfile, objective));
                                            });
                                            TaskQueue queue = new TaskQueue(runnables);
                                            queue.startTasks();
                                        } else {
                                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
                                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
                                            if (objective.shouldDisplayNextObjectiveTitle())
                                                player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 80, 10);  // Display a title on the screen
                                            Plugin.updatePlayerCachedLocations(player);
                                            Bukkit.getPluginManager().callEvent(new QuestCompleteObjectiveEvent(quest, questProfile, objective));
                                        }
                                    } else { // If we have completed the quest
                                        quest.getQuestState().setCompleted(true);
                                        questProfile.save(questProfile.getQuestPoints() + quest.getRewards().getQuestPointsReward());
                                        if (objective.hasCompletedMessage()) { // If we have a completed message, create a task queue and add completed message, and rewards to queue
                                            List<Runnable> runnables = new ArrayList<>();
                                            SpeechParser speechParser = new SpeechParser(player);
                                            for (String message : objective.getCompletedMessage()) {
                                                runnables.add(() -> {
                                                    speechParser.updateParsedMessage(message);
                                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', speechParser.getParsedMessage()));
                                                    speechParser.executeCommands();
                                                });
                                            }
                                            TaskQueue queue = new TaskQueue(runnables);
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
                                                    player.sendTitle(ChatColor.GOLD + "Quest Complete!", ChatColor.YELLOW + quest.getQuestName(), 10, 80, 10); // Send a goal message title
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
                                                player.sendTitle(ChatColor.GOLD + "Quest Complete!", ChatColor.YELLOW + quest.getQuestName(), 10, 80, 10); // Send a goal message title
                                        }
                                        if (quest.getRewards().hasExecute()) { // Execute quest commands
                                            quest.getRewards().executeCommand(player.getName());
                                        }
                                        RunicCoreHook.giveRewards(player, quest.getRewards()); // Give rewards
                                        Bukkit.getServer().getPluginManager().callEvent(new QuestCompleteEvent(quest, questProfile)); // Call the quest event
                                    }
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onKill(MythicMobDeathEvent event) {
        if (!(event.getKiller() instanceof Player)) return;
        Party party = RunicCore.getPartyManager().getPlayerParty(((Player) event.getKiller()));
        if (party != null) {
            for (Player player : party.getMembersWithLeader()) {
                if (player.getLocation().getWorld() != event.getKiller().getLocation().getWorld()) continue;
                if (player.getLocation().distanceSquared(event.getKiller().getLocation()) > KILL_COUNT_DIST_SQUARED)
                    continue;
                runMythicMobsKill(player, event.getMobType());
            }
        } else {
            runMythicMobsKill((Player) event.getKiller(), event.getMobType());
        }
    }

}