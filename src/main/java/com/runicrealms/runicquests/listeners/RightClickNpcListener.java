package com.runicrealms.runicquests.listeners;

import com.runicrealms.plugin.Npc;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.NpcClickEvent;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.api.QuestCompleteEvent;
import com.runicrealms.runicquests.api.QuestCompleteObjectiveEvent;
import com.runicrealms.runicquests.api.QuestStartEvent;
import com.runicrealms.runicquests.model.QuestProfileData;
import com.runicrealms.runicquests.quests.*;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveHandler;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;
import com.runicrealms.runicquests.task.HologramTaskQueue;
import com.runicrealms.runicquests.task.TaskQueue;
import com.runicrealms.runicquests.util.QuestsUtil;
import com.runicrealms.runicquests.util.SpeechParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.*;

/**
 * Handles npc clicks for the RunicQuests plugin. May include starting a new quest,
 * progressing a 'talk' objective, or idle messages
 *
 * @author Excel_, Skyfallin
 */
public class RightClickNpcListener implements Listener, QuestObjectiveHandler {

    /**
     * Checks if the player has valid 'talk' objectives, then checks if this is the right npc
     *
     * @return true if a valid talk object was progressed
     */
    private boolean attemptToTalkToNpc(Player player, Npc npc, QuestProfileData profileData, int slot, HashMap<Long, TaskQueue> npcTaskQueues) {
        for (Quest quest : profileData.getQuestsMap().get(slot)) {
            if (!isQuestActive(quest)) continue;
            for (QuestObjective objective : quest.getObjectives()) {
                if (!isValidObjective(quest, objective, QuestObjectiveType.TALK)) continue;
                QuestObjectiveTalk talkObjective = (QuestObjectiveTalk) objective;
                if (!talkObjective.getQuestNpc().getNpcId().equals(npc.getId())) continue;
                progressTalkObjective(player, profileData, quest, objective, npcTaskQueues);
                return true;
            }
        }
        return false;
    }

    /**
     * Progresses the objective
     */
    private void completeTalkObjective(Player player, QuestObjective objective) {
        // Objective complete!
        objective.setCompleted(true);
        if (objective.hasExecute()) { // Executes the objective commands
            objective.executeCommand(player.getName());
        }
        if (objective.hasCompletedMessage()) { // Display completed message if there is one (in chat)
            for (String message : objective.getCompletedMessage()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', new SpeechParser(player, message).getParsedMessage()));
            }
        }
    }

    /**
     * Loops through quests to find a FirstNPC match for the NPC.
     * If there are multiple quests, tries to return the lowest-level quest instead.
     * This is a band-aid feature for FirstNpcs that give multiple quests
     *
     * @param npc         to check
     * @param profileData of the player
     * @param slot        of the character
     * @return a Quest if it has this NPC as its first, else null
     */
    private Quest findQuestFromNpcGiver(Npc npc, QuestProfileData profileData, int slot) {
        List<Quest> results = new ArrayList<>();
        for (Quest quest : profileData.getQuestsMap().get(slot)) {
            if (quest.getFirstNPC().getNpcId() == npc.getId()) {
                if (quest.getQuestState().isCompleted()) continue; // Ignore completed quests
                results.add(quest);
            }
        }
        // Sort the results by level to find the lowest level quest
        results.sort(Comparator.comparing(quest -> quest.getRequirements().getClassLvReq()));
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    /**
     * Handles logic for an NPC click with a 'FirstNpc'. (A quest-giver)
     * There are 2 cases: quest not started, or quest in-progress
     */
    private void handleFirstNpcQuest(Player player, int slot, Npc npc, QuestProfileData profileData,
                                     Quest quest, HashMap<Long, TaskQueue> npcTaskQueues) {
        if (!quest.getQuestState().hasStarted()) {
            // Disable repeatable quests on CD
            if (quest.isRepeatable() && !QuestsUtil.canStartRepeatableQuest(player.getUniqueId(), quest)) {
                String time = QuestsUtil.repeatableQuestTimeRemaining(player, quest);
                player.sendMessage(questCooldownMessage(time));
                return;
            }
            handleQuestNotStarted(player, profileData, quest, npcTaskQueues);
        } else if (quest.getQuestState().hasStarted() && !quest.getQuestState().isCompleted()) {
            attemptToTalkToNpc(player, npc, profileData, slot, npcTaskQueues);
        }
    }

    /**
     * Logic for a quest which has not started. Checks requirements to see if quest can begin
     */
    private void handleQuestNotStarted(Player player, QuestProfileData profileData, Quest quest, HashMap<Long, TaskQueue> npcTaskQueues) {
        RequirementsResult result = Quest.hasMetRequirements(player, quest);
        if (result != RequirementsResult.ALL_REQUIREMENTS_MET) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            List<String> requirementsNotMetMsg = Quest.getRequirementsNotMetMsg(quest, result);
            if (!requirementsNotMetMsg.isEmpty()) {
                HologramTaskQueue queue = new HologramTaskQueue
                        (
                                HologramTaskQueue.QuestResponse.REQUIREMENTS_NOT_MET,
                                quest,
                                quest.getFirstNPC().getNpcId(),
                                quest.getFirstNPC().getLocation(),
                                player,
                                requirementsNotMetMsg
                        ); // Create a task queue with the quests completed not met message
                queue.setCompletedTask(() -> npcTaskQueues.remove(quest.getFirstNPC().getId()));
                npcTaskQueues.put(quest.getFirstNPC().getId(), queue);
                queue.startTasks();
            }
            return;
        }
        // Reset repeatable quest objectives
        if (quest.isRepeatable()) {
            resetRepeatableQuest(quest);
        }
        // Requirements met!
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10.0f, 1.00f); // Play sound
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 10.0f, 1.0f); // Play sound
        quest.getQuestState().setStarted(true);
        RunicQuests.getLocationManager().updatePlayerCachedLocations(player);
        if (quest.getFirstNPC().hasExecute()) { // Execute the first NPC commands
            quest.getFirstNPC().executeCommand(player.getName());
        }
        // Create a task queue with the first NPC speech
        HologramTaskQueue queue = new HologramTaskQueue
                (
                        HologramTaskQueue.QuestResponse.STARTED,
                        quest,
                        quest.getFirstNPC().getNpcId(),
                        quest.getFirstNPC().getLocation(),
                        player,
                        quest.getFirstNPC().getSpeech()
                );
        queue.setCompletedTask(() -> npcTaskQueues.remove(quest.getFirstNPC().getId()));
        queue.addTasks(() -> {
            quest.getFirstNPC().setState(FirstNpcState.ACCEPTED);
            queue.getHologram().delete();
            String goalMessage = ChatColor.translateAlternateColorCodes('&', QuestObjective.getObjective(quest.getObjectives(), 1).getGoalMessage());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
            player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 80, 10);
            RunicQuests.getLocationManager().updatePlayerCachedLocations(player);
            // Call our start event
            Bukkit.getPluginManager().callEvent(new QuestStartEvent(quest, profileData, quest.getFirstNPC()));
        });
        npcTaskQueues.put(quest.getFirstNPC().getId(), queue);
        queue.startTasks();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onNpcRightClick(NpcClickEvent event) {
        Npc npc = event.getNpc();
        Player player = event.getPlayer();
        int slot = RunicCore.getCharacterAPI().getCharacterSlot(player.getUniqueId());
        QuestProfileData profileData = RunicQuests.getAPI().getQuestProfile(player.getUniqueId());
        // Static map that keeps track of the current talk operation
        HashMap<Long, TaskQueue> npcTaskQueues = RunicQuests.getNpcTaskQueues();
        boolean foundTalkObjective = attemptToTalkToNpc(player, npc, profileData, slot, npcTaskQueues);
        if (foundTalkObjective) return;
        // If we are not talking to this NPC for a quest, then check if they have a new quest
        Quest questMatchingFirstNpc = findQuestFromNpcGiver(npc, profileData, slot);
        if (questMatchingFirstNpc != null) {
            // If the player is currently talking with the NPC, simply move to next speech line
            if (npcTaskQueues.containsKey(questMatchingFirstNpc.getFirstNPC().getId())) {
                npcTaskQueues.get(questMatchingFirstNpc.getFirstNPC().getId()).nextTask();
            } else {
                handleFirstNpcQuest(player, slot, npc, profileData, questMatchingFirstNpc, npcTaskQueues);
            }
        }
    }

    /**
     * Progresses an objective to talk to NPC
     */
    private void progressTalkObjective(Player player, QuestProfileData profileData,
                                       Quest quest, QuestObjective objective, HashMap<Long, TaskQueue> npcTaskQueues) {
        QuestObjectiveTalk talkObjective = (QuestObjectiveTalk) objective;
        // If the player is currently talking with the NPC, simply move to next speech line
        if (npcTaskQueues.containsKey(talkObjective.getQuestNpc().getId())) {
            npcTaskQueues.get(talkObjective.getQuestNpc().getId()).nextTask();
            return;
        }
        // Collect items if needed
        if (objective.requiresQuestItem()) { // Check for a required quest item, remove it from inventory
            if (RunicQuests.hasQuestItems(objective, player)) {
                for (QuestItem questItem : objective.getQuestItems()) {
                    RunicQuests.removeItem(player, questItem.getItemName(), questItem.getItemType(), questItem.getAmount());
                }
                player.updateInventory();
            } else {
                if (talkObjective.getQuestNpc().hasDeniedMessage()) {
                    HologramTaskQueue queue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.REQUIREMENTS_NOT_MET, quest, talkObjective.getQuestNpc().getNpcId(), QuestNpc.getQuestNpcLocation(talkObjective.getQuestNpc()), player, talkObjective.getQuestNpc().getDeniedMessage());
                    queue.setCompletedTask(() -> npcTaskQueues.remove(talkObjective.getQuestNpc().getId()));
                    npcTaskQueues.put(talkObjective.getQuestNpc().getId(), queue);
                    queue.startTasks();
                }
                return;
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
        HologramTaskQueue queue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.STARTED, quest, talkObjective.getQuestNpc().getNpcId(), QuestNpc.getQuestNpcLocation(talkObjective.getQuestNpc()), player, talkObjective.getQuestNpc().getSpeech());
        queue.setCompletedTask(() -> npcTaskQueues.remove(talkObjective.getQuestNpc().getId()));

        if (!Objects.equals(objective.getObjectiveNumber(), QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber())) { // Check that this is not the last objective
            // Add the new objective message to the task queue
            queue.addTasks(() -> {
                completeTalkObjective(player, objective);
                queue.getHologram().delete();
                String goalMessage = ChatColor.translateAlternateColorCodes('&', QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
                if (objective.shouldDisplayNextObjectiveTitle())
                    player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 80, 10); // Send a goal message title
                RunicQuests.getLocationManager().updatePlayerCachedLocations(player);
                Bukkit.getPluginManager().callEvent(new QuestCompleteObjectiveEvent(quest, profileData, objective));
            });
        } else { // If this is the last objective
            queue.addTasks(() -> {
                completeTalkObjective(player, objective);
                queue.getHologram().delete();
                Bukkit.getServer().getPluginManager().callEvent(new QuestCompleteEvent(profileData, quest, objective)); // Fire the quest completed event
            });
        }

        npcTaskQueues.put(talkObjective.getQuestNpc().getId(), queue); // Add the queue to the NPCs that are being talked to
        queue.startTasks();
    }

    private String questCooldownMessage(String time) {
        return ChatColor.translateAlternateColorCodes('&', "&3&lON COOLDOWN: &e" + time);
    }

    /**
     * Reset all the objectives for a repeatable quest
     */
    private void resetRepeatableQuest(Quest quest) {
        for (QuestObjective objective : quest.getObjectives()) {
            objective.setCompleted(false);
            objective.resetObjective();
        }
    }

}
