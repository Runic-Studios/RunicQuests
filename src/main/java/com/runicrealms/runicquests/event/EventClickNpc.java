package com.runicrealms.runicquests.event;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.api.QuestCompleteEvent;
import com.runicrealms.runicquests.api.RunicQuestsAPI;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.event.custom.RightClickNpcEvent;
import com.runicrealms.runicquests.quests.*;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveBreak;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveSlay;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;
import com.runicrealms.runicquests.task.HologramTaskQueue;
import com.runicrealms.runicquests.task.TaskQueue;
import com.runicrealms.runicquests.util.RunicCoreHook;
import com.runicrealms.runicquests.util.SpeechParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public class EventClickNpc implements Listener {

    @EventHandler
    public void onNpcRightClick(RightClickNpcEvent event) {
        Player player = event.getPlayer();
        QuestProfile questProfile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId());
        HashMap<Long, TaskQueue> npcTaskQueues = Plugin.getNpcTaskQueues();
        if (questProfile == null) return;
        questsLoop:
        for (Quest quest : questProfile.getQuests()) { // Loop through quests to find a match for the NPC
            if (quest.getQuestState().isCompleted() && !quest.isRepeatable()) { // Check for if the quest is completed
                if (quest.getFirstNPC().getNpcId().equals(event.getNpcId())) { // Check for first NPC quest completed speech
                    if (quest.getFirstNPC().hasQuestCompletedSpeech()) { // Create a task queue for the speech
                        HologramTaskQueue queue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.COMPLETED, quest.getFirstNPC().getLocation(), player, quest.getFirstNPC().getQuestCompletedSpeech());
                        queue.setCompletedTask(() -> npcTaskQueues.remove(quest.getFirstNPC().getId()));
                        npcTaskQueues.put(quest.getFirstNPC().getId(), queue);
                        queue.startTasks();
                        continue;
                    }
                }
            }
            if (quest.getQuestState().hasStarted()) { // If the quest has started...
                for (QuestObjective objective : quest.getObjectives()) { // Loop through the objectives
                    if (objective.getObjectiveType() == QuestObjectiveType.TALK) { // Check the objective type
                        QuestObjectiveTalk talkObjective = (QuestObjectiveTalk) objective;
                        if (talkObjective.getQuestNpc().getNpcId().equals(event.getNpcId())) { // Check that the NPC id matches the one that has been clicked
                            if (talkObjective.getQuestNpc().getNpcId().equals(quest.getFirstNPC().getNpcId())) { // Check if the NPC being talked to is the first NPC (same NPC used twice)
                                if (npcTaskQueues.containsKey(quest.getFirstNPC().getId())) { // If you are talking to the first NPC, continue to next objective
                                    continue;
                                }
                            }
                            if (npcTaskQueues.containsKey(talkObjective.getQuestNpc().getId())) { // If you are talking to the NPC...
                                npcTaskQueues.get(talkObjective.getQuestNpc().getId()).nextTask(); // Move to next speech line
                                return;
                            }
                            if (!objective.isCompleted()) { // Check that the objective isn't completed
                                if (objective.getObjectiveNumber() != 1) { // Check that the previous objective has been completed
                                    QuestObjective previousObjective = QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 1);
                                    if (!previousObjective.isCompleted()) {
                                        if (objective.getObjectiveNumber() != 2) {
                                            if (!QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 2).isCompleted()) {
                                                continue;
                                            }
                                        }
                                        if (!(previousObjective instanceof QuestObjectiveTalk)) {
                                            if (talkObjective.getQuestNpc().hasDeniedMessage()) {
                                                if (!talkObjective.requiresQuestItem()) {
                                                    HologramTaskQueue queue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.REQUIREMENTS_NOT_MET, QuestNpc.getQuestNpcLocation(talkObjective.getQuestNpc()), player, talkObjective.getQuestNpc().getDeniedMessage());
                                                    queue.setCompletedTask(() -> npcTaskQueues.remove(talkObjective.getQuestNpc().getId()));
                                                    npcTaskQueues.put(talkObjective.getQuestNpc().getId(), queue);
                                                    queue.startTasks();
                                                    break questsLoop;
                                                }
                                            }
                                        }
                                        continue;
                                    }
                                }
                                if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) { // Check that you have accepted the quest
                                    continue;
                                }
                                if (objective.requiresQuestItem()) { // Check for a required quest item, remove it from inventory
                                    if (Plugin.hasQuestItems(objective, player)) {
                                        for (QuestItem questItem : objective.getQuestItems()) {
                                            Plugin.removeItem(player, questItem.getItemName(), questItem.getItemType(), questItem.getAmount());
                                        }
                                        player.updateInventory();
                                    } else {
                                        if (talkObjective.getQuestNpc().hasDeniedMessage()) {
                                            HologramTaskQueue queue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.REQUIREMENTS_NOT_MET, QuestNpc.getQuestNpcLocation(talkObjective.getQuestNpc()), player, talkObjective.getQuestNpc().getDeniedMessage());
                                            queue.setCompletedTask(() -> npcTaskQueues.remove(talkObjective.getQuestNpc().getId()));
                                            npcTaskQueues.put(talkObjective.getQuestNpc().getId(), queue);
                                            queue.startTasks();
                                        }
                                        break questsLoop;
                                    }
                                }
                                objective.setCompleted(true);
                                questProfile.save();
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 0); // Play sound
                                if (objective.hasExecute()) { // Execute objective commands
                                    objective.executeCommand(player.getName());
                                }
                                if (objective.hasCompletedMessage()) { // Display completed message if there is one
                                    // todo: holotaskqueue somewhere here? cuz this needs a hologram? idk
                                    for (String message : objective.getCompletedMessage()) {
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', new SpeechParser(message, player).getParsedMessage()));
                                    }
                                }
                                HologramTaskQueue queue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.STARTED, QuestNpc.getQuestNpcLocation(talkObjective.getQuestNpc()), player, talkObjective.getQuestNpc().getSpeech());
                                queue.setCompletedTask(() -> npcTaskQueues.remove(talkObjective.getQuestNpc().getId()));
                                if (!Objects.equals(objective.getObjectiveNumber(), QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber())) { // Check that this is not the last objective
                                    // Add the new objective message to the task queue
                                    queue.addTasks(() -> {
                                        queue.getHologram().delete();
                                        String goalMessage = ChatColor.translateAlternateColorCodes('&', QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage());
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
                                        if (objective.shouldDisplayNextObjectiveTitle())
                                            player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 80, 10); // Send a goal message title
                                        Plugin.updatePlayerCachedLocations(player);
                                    });
                                } else { // If this is the last objective then...
                                    // Add the quest rewards to the task queue
                                    queue.addTasks(() -> {
                                        queue.getHologram().delete();
                                        if (quest.isRepeatable()) { // If the quest is repeatable, then handle the state management (cooldowns handled in CompleteListener)
                                            quest.getQuestState().setStarted(false);
                                            quest.getFirstNPC().setState(FirstNpcState.NEUTRAL);
                                        } else {
                                            quest.getQuestState().setCompleted(true);
                                        }
                                        questProfile.save(questProfile.getQuestPoints() + quest.getRewards().getQuestPointsReward());
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
                                        if (quest.getRewards().hasExecute()) { // Execute the quest rewards commands
                                            quest.getRewards().executeCommand(player.getName());
                                        }
                                        RunicCoreHook.giveRewards(player, quest.getRewards()); // Give the rewards
                                        Bukkit.getServer().getPluginManager().callEvent(new QuestCompleteEvent(quest, questProfile)); // Fire the quest completed event
                                    });
                                }
                                npcTaskQueues.put(talkObjective.getQuestNpc().getId(), queue); // Add the queue to the NPCs that are being talked to
                                queue.startTasks();
                                return;
                            }
                        }
                    }
                }
            }
        }
        for (Quest quest : questProfile.getQuests()) {
            if ((!quest.getQuestState().isCompleted()) ||
                    (quest.isRepeatable() && quest.getQuestState().hasStarted() && quest.getQuestState().isCompleted())) { // Check that the quest is not completed
                if ((quest.getFirstNPC().getNpcId().equals(event.getNpcId()))
                        && Plugin.canStartRepeatableQuest(event.getPlayer().getUniqueId(), quest.getQuestID())) { // Check for an NPC id match between the first NPC and the clicked NPC
                    if (!QuestObjective.getObjective(quest.getObjectives(), 1).isCompleted() || quest.isRepeatable()) { // Check that the first objective has not been completed
                        if (!npcTaskQueues.containsKey(quest.getFirstNPC().getId())) { // Check that the player is not currently talking with the NPC
                            if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED || (quest.isRepeatable() && Plugin.allObjectivesComplete(quest))) { // Check that the player has not yet accepted the quest
                                if (quest.isRepeatable()) { // Check if the quest is repeatable
                                    for (QuestObjective qobjective : quest.getObjectives()) { // Reset all the objective stats (mobs killed and blocks broken)
                                        qobjective.setCompleted(false);
                                        if (qobjective.getObjectiveType() == QuestObjectiveType.SLAY) {
                                            ((QuestObjectiveSlay) qobjective).setMobsKilled(0);
                                        }
                                        if (qobjective.getObjectiveType() == QuestObjectiveType.BREAK) {
                                            if (((QuestObjectiveBreak) qobjective).hasBlockAmount()) {
                                                ((QuestObjectiveBreak) qobjective).setBlocksBroken(0);
                                            }
                                        }
                                    }
                                    questProfile.save();
                                }
                                boolean meetsRequirements = true;
                                if (quest.getRequirements().hasCompletedQuestRequirement()) { // Check if the quest has a completed quests requirement
                                    if (!RunicCoreHook.hasCompletedRequiredQuests(player, quest.getRequirements().getCompletedQuestsRequirement())) { // Check that player has completed the required quests
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                                        meetsRequirements = false;
                                        if (quest.getRequirements().hasCompletedQuestsNotMetMsg()) {
                                            HologramTaskQueue queue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.REQUIREMENTS_NOT_MET, quest.getFirstNPC().getLocation(), player, quest.getRequirements().getCompletedQuestsNotMetMsg()); // Create a task queue with the quests completed not met message
                                            queue.setCompletedTask(() -> npcTaskQueues.remove(quest.getFirstNPC().getId()));
                                            npcTaskQueues.put(quest.getFirstNPC().getId(), queue);
                                            queue.startTasks();
                                        }
                                    }
                                }
                                if (meetsRequirements) {
                                    if (!RunicCoreHook.isReqClassLv(player, quest.getRequirements().getClassLvReq())) { // Check that the player is the required level
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                                        meetsRequirements = false;
                                        if (quest.getRequirements().hasLevelNotMetMsg()) {
                                            HologramTaskQueue queue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.REQUIREMENTS_NOT_MET, quest.getFirstNPC().getLocation(), player, quest.getRequirements().getLevelNotMetMsg()); // Create a task queue with the level not met message
                                            queue.setCompletedTask(() -> npcTaskQueues.remove(quest.getFirstNPC().getId()));
                                            npcTaskQueues.put(quest.getFirstNPC().getId(), queue);
                                            queue.startTasks();
                                        }
                                    }
                                }
                                if (meetsRequirements) {
                                    if (quest.getRequirements().hasCraftingRequirement()) { // Check if the quest has a crafting requirement
                                        for (CraftingProfessionType profession : quest.getRequirements().getCraftingProfessionType()) {
                                            if (!RunicCoreHook.isRequiredCraftingLevel(player, profession, quest.getRequirements().getCraftingRequirement())) { // Check that the player is the required crafting level
                                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                                                meetsRequirements = false;
                                                if (quest.getRequirements().hasCraftingLevelNotMetMsg()) {
                                                    HologramTaskQueue queue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.REQUIREMENTS_NOT_MET, quest.getFirstNPC().getLocation(), player, quest.getRequirements().getCraftingLevelNotMetMsg());
                                                    queue.setCompletedTask(() -> npcTaskQueues.remove(quest.getFirstNPC().getId()));
                                                    npcTaskQueues.put(quest.getFirstNPC().getId(), queue);
                                                    queue.startTasks();
                                                }
                                            }
                                        }
                                    }
                                }
                                if (meetsRequirements) {
                                    if (quest.getRequirements().hasClassTypeRequirement()) { // Check if the quest has a class requirement
                                        if (!RunicCoreHook.isRequiredClass(quest.getRequirements().getClassTypeRequirement(), player)) { // Check that the player is the required class
                                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                                            meetsRequirements = false;
                                            if (quest.getRequirements().hasClassNotMetMsg()) {
                                                HologramTaskQueue queue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.REQUIREMENTS_NOT_MET, quest.getFirstNPC().getLocation(), player, quest.getRequirements().getClassTypeNotMetMsg());
                                                queue.setCompletedTask(() -> npcTaskQueues.remove(quest.getFirstNPC().getId()));
                                                npcTaskQueues.put(quest.getFirstNPC().getId(), queue);
                                                queue.startTasks();
                                            }
                                        }
                                    }
                                }
                                if (meetsRequirements) { // Check that the player meets the requirements
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 0); // Play sound
                                    quest.getQuestState().setStarted(true);
                                    questProfile.save();
                                    Plugin.updatePlayerCachedLocations(player);
                                    if (quest.getFirstNPC().hasExecute()) { // Execute the first NPC commands
                                        quest.getFirstNPC().executeCommand(player.getName());
                                    }
                                    HologramTaskQueue queue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.STARTED, quest.getFirstNPC().getLocation(), player, quest.getFirstNPC().getSpeech()); // Create a task queue with the first NPC speech
                                    queue.setCompletedTask(() -> {
                                        npcTaskQueues.remove(quest.getFirstNPC().getId());
                                        quest.getFirstNPC().setState(FirstNpcState.ACCEPTED);
                                        questProfile.save();
                                    });
                                    queue.addTasks(() -> {
                                        queue.getHologram().delete();
                                        String goalMessage = ChatColor.translateAlternateColorCodes('&', QuestObjective.getObjective(quest.getObjectives(), 1).getGoalMessage());
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
                                        player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 80, 10); // Send a goal message title
                                        Plugin.updatePlayerCachedLocations(player);
                                    });
                                    npcTaskQueues.put(quest.getFirstNPC().getId(), queue);
                                    queue.startTasks();
                                    return;
                                } else {
                                    break;
                                }
                            }
                        } else { // If the player IS talking to this NPC currently, then...
                            npcTaskQueues.get(quest.getFirstNPC().getId()).nextTask(); // Move to next speech line
                            return;
                        }
                    }
                }
                if ((quest.getFirstNPC().getNpcId().equals(event.getNpcId()))
                        && !Plugin.canStartRepeatableQuest(event.getPlayer().getUniqueId(), quest.getQuestID())) { // If the player is waiting on a quest cooldown (repeatable quests)
                    String time = RunicQuestsAPI.repeatableQuestTimeRemaining(player, quest.getQuestID());
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7You must wait " + time + " before completing this quest again!"));
                }
            }
        }
        for (Quest quest : questProfile.getQuests()) { // Loop through the quests, check for idle messages
            for (QuestObjective objective : quest.getObjectives()) { // Loop through objectives
                if (objective.getObjectiveType() == QuestObjectiveType.TALK) { // Check for objective of type talk
                    QuestObjectiveTalk talkObjective = (QuestObjectiveTalk) objective;
                    if (talkObjective.getQuestNpc().getNpcId().equals(event.getNpcId())) { // Check that the NPC id matches the one clicked on
                        if (talkObjective.getQuestNpc().hasIdleSpeech()) { // Check for idle speech
                            idleMessageLoop:
                            // outer loop label
                            for (QuestIdleMessage idleMessage : talkObjective.getQuestNpc().getIdleSpeech()) { // Loop through idle messages
                                if (idleMessage.getConditions().hasQuestCompleted()) { // Check for quest completed condition
                                    if (quest.getQuestState().isCompleted() != idleMessage.getConditions().getQuestCompleted()) {
                                        continue;
                                    }
                                }
                                if (idleMessage.getConditions().hasQuestStarted()) { // Check for quest started condition
                                    if (quest.getQuestState().hasStarted() != idleMessage.getConditions().getQuestStarted()) {
                                        continue;
                                    }
                                }
                                if (idleMessage.getConditions().hasObjectiveStates()) { // Check for objectives completed condition
                                    for (int i = 1; i <= quest.getObjectives().size(); i++) { // for every quest objective
                                        if (idleMessage.getConditions().getObjectiveStates().size() - 1 >= i) {
                                            if (idleMessage.getConditions().getObjectiveStates().get(i) != null) {
                                                int finalI = i;
                                                Optional<QuestObjective> questObjective = quest.getObjectives().stream().filter(obj -> obj.getObjectiveNumber() == finalI).findFirst();
                                                if (questObjective.isPresent() && questObjective.get().isCompleted() != idleMessage.getConditions().getObjectiveStates().get(i)) {
                                                    continue idleMessageLoop;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (objective.requiresQuestItem()) {
                                    if (idleMessage.getConditions().hasRequiresQuestItems()) { // Check for requires quest items condition
                                        if (Plugin.hasQuestItems(objective, player) != idleMessage.getConditions().requiresQuestItems()) {
                                            continue;
                                        }
                                    }
                                }
                                HologramTaskQueue queue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.STARTED, QuestNpc.getQuestNpcLocation(talkObjective.getQuestNpc()), player, idleMessage.getSpeech());
                                queue.setCompletedTask(() -> npcTaskQueues.remove(talkObjective.getQuestNpc().getId()));
                                npcTaskQueues.put(talkObjective.getQuestNpc().getId(), queue);
                                queue.startTasks();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

}
