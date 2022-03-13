//package com.runicrealms.runicquests.command;
//
//import com.runicrealms.plugin.RunicCore;
//import com.runicrealms.plugin.character.api.CharacterApi;
//import com.runicrealms.plugin.party.Party;
//import com.runicrealms.runicquests.Plugin;
//import com.runicrealms.runicquests.api.QuestCompleteEvent;
//import com.runicrealms.runicquests.data.PlayerDataLoader;
//import com.runicrealms.runicquests.data.QuestProfile;
//import com.runicrealms.runicquests.quests.Quest;
//import com.runicrealms.runicquests.quests.QuestItem;
//import com.runicrealms.runicquests.quests.objective.QuestObjective;
//import com.runicrealms.runicquests.quests.objective.QuestObjectiveTrigger;
//import com.runicrealms.runicquests.quests.trigger.Trigger;
//import com.runicrealms.runicquests.quests.trigger.TriggerObjectiveHandler;
//import com.runicrealms.runicquests.task.HologramTaskQueue;
//import com.runicrealms.runicquests.task.TaskQueue;
//import com.runicrealms.runicquests.util.RunicCoreHook;
//import com.runicrealms.runicquests.util.SpeechParser;
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.Sound;
//import org.bukkit.command.BlockCommandSender;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//public class QuestTriggerCommand implements CommandExecutor {
//
//    @Override
//    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//        if (sender.isOp()) {
//            Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
//                if (args.length == 3) {
//                    Set<Player> players = new HashSet<>();
//                    if (args[0].equalsIgnoreCase("player")) {
//                        Player player = null;
//                        if (args[1].equalsIgnoreCase("@p")) {
//                            if (sender instanceof BlockCommandSender) {
//                                BlockCommandSender blockSender = (BlockCommandSender) sender;
//                                double current = -1;
//                                for (Player target : Bukkit.getOnlinePlayers()) {
//                                    double distanceSquared = target.getLocation().distanceSquared(blockSender.getBlock().getLocation());
//                                    if (current == -1 || target.getLocation().distanceSquared(blockSender.getBlock().getLocation()) < current) {
//                                        current = distanceSquared;
//                                        player = target;
//                                    }
//                                }
//                            } else {
//                                sender.sendMessage(ChatColor.RED + "This can only be run from a command block!");
//                                return;
//                            }
//                        } else {
//                            player = Bukkit.getPlayerExact(args[1]);
//                        }
//                        if (player != null) {
//                            players.add(player);
//                        } else {
//                            sender.sendMessage(ChatColor.RED + "That player doesn't exist!");
//                            return;
//                        }
//                    } else if (args[0].equalsIgnoreCase("party")) {
//                        Player partyMember = null;
//                        if (args[1].equalsIgnoreCase("@p")) {
//                            if (sender instanceof BlockCommandSender) {
//                                BlockCommandSender blockSender = (BlockCommandSender) sender;
//                                double current = -1;
//                                for (Player target : Bukkit.getOnlinePlayers()) {
//                                    double distanceSquared = target.getLocation().distanceSquared(blockSender.getBlock().getLocation());
//                                    if (target.getLocation().getWorld() == blockSender.getBlock().getWorld()) {
//                                        if (current == -1 || target.getLocation().distanceSquared(blockSender.getBlock().getLocation()) < current) {
//                                            current = distanceSquared;
//                                            partyMember = target;
//                                        }
//                                    }
//                                }
//                            } else {
//                                sender.sendMessage(ChatColor.RED + "This can only be run from a command block!");
//                                return;
//                            }
//                        } else {
//                            partyMember = Bukkit.getPlayerExact(args[1]);
//                        }
//                        if (partyMember != null) {
//                            Party party = RunicCore.getPartyManager().getPlayerParty(partyMember);
//                            if (party != null) {
//                                players.addAll(party.getMembersWithLeader());
//                            } else {
//                                players.add(partyMember);
//                            }
//                        } else {
//                            sender.sendMessage(ChatColor.RED + "That player doesn't exist!");
//                            return;
//                        }
//                    } else {
//                        sender.sendMessage(ChatColor.RED + "Bad syntax! /questtrigger player|party <party-member-name|player-name|@p> <trigger-id>");
//                        return;
//                    }
//                    String triggerId = args[2];
//                    Trigger trigger = TriggerObjectiveHandler.getTrigger(triggerId);
//                    if (trigger != null) {
//                        for (Player player : players) {
//                            QuestProfile profile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId());
//                            int characterSlot = CharacterApi.getCurrentCharacterSlot(player);
//                            for (Quest quest : profile.getQuests()) {
//                                if (quest.getQuestID() == trigger.getQuestId()) {
//                                    if (!quest.getQuestState().isCompleted()) {
//                                        QuestObjectiveTrigger objective = (QuestObjectiveTrigger) QuestObjective.getObjective(quest.getObjectives(), trigger.getObjectiveId());
//                                        if (objective == null) {
//                                            continue;
//                                        }
//                                        if (objective.isCompleted()) {
//                                            continue;
//                                        }
//                                        if (trigger.getObjectiveId() != 1) {
//                                            if (!QuestObjective.getObjective(quest.getObjectives(), trigger.getObjectiveId() - 1).isCompleted()) {
//                                                continue;
//                                            }
//                                        }
//                                        if (objective.requiresQuestItem()) { // Check if the objective requires a quest item, remove if there is one
//                                            if (Plugin.hasQuestItems(objective, player)) {
//                                                for (QuestItem questItem : objective.getQuestItems()) {
//                                                    Plugin.removeItem(player, questItem.getItemName(), questItem.getItemType(), questItem.getAmount());
//                                                }
//                                                player.updateInventory();
//                                            } else {
//                                                continue;
//                                            }
//                                        }
//                                        objective.setCompleted(true);
//                                        profile.save();
//                                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 0); // Play sound
//                                        if (objective.hasExecute()) { // Execute objective commands
//                                            objective.executeCommand(player.getName());
//                                        }
//                                        if (!objective.getObjectiveNumber().equals(QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber())) { // If this is not the last objective
//                                            String goalMessage = ChatColor.translateAlternateColorCodes('&', QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage()); // Get goal message
//                                            if (objective.hasCompletedMessage()) { // If the objective has a completed message
//                                                List<Runnable> runnables = new ArrayList<>();
//                                                for (String message : objective.getCompletedMessage()) { // Put the completed message into a task queue
//                                                    runnables.add(() -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', new SpeechParser(message, player).getParsedMessage())));
//                                                }
//                                                // Put the goal message into the task queue
//                                                runnables.add(() -> {
//                                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
//                                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
//                                                    player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 80, 10); // Display goal message title
//                                                    Plugin.updatePlayerCachedLocations(player);
//                                                });
//                                                HologramTaskQueue hologramTaskQueue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.STARTED, idk, objective.getCompletedMessage(), player);
//                                                hologramTaskQueue.startTasks();
//                                            } else {
//                                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
//                                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
//                                                player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 80, 10);  // Display a title on the screen
//                                                Plugin.updatePlayerCachedLocations(player);
//                                            }
//                                        } else { // If we have completed the quest...
//                                            quest.getQuestState().setCompleted(true);
//                                            profile.save(profile.getQuestPoints() + quest.getRewards().getQuestPointsReward());
//                                            List<Runnable> runnables = new ArrayList<>();
//                                            for (String message : objective.getSpeech()) { // Create a task queue with the completed message
//                                                runnables.add(() -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', new SpeechParser(message, player).getParsedMessage())));
//                                            }
//                                            TaskQueue queue = new TaskQueue(runnables);
//                                            // Add the quest rewards to the task queue
//                                            queue.addTasks(() -> {
//                                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1); // Play sound
//                                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
//                                                if (quest.getRewards().getQuestPointsReward() != 0)
//                                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() == 1 ? "" : "s")));
//                                                if (quest.getRewards().getMoneyReward() != 0)
//                                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() == 1 ? "" : "s")));
//                                                if (quest.getRewards().getExperienceReward() != 0)
//                                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience Point" + (quest.getRewards().getExperienceReward() == 1 ? "" : "s")));
//                                                player.sendTitle(ChatColor.GOLD + "Quest Complete!", ChatColor.YELLOW + quest.getQuestName(), 10, 80, 10); // Send a goal message title
//                                            });
//                                            queue.startTasks();
//                                            if (quest.getRewards().hasExecute()) { // Execute rewards commands
//                                                quest.getRewards().executeCommand(player.getName());
//                                            }
//                                            RunicCoreHook.giveRewards(player, quest.getRewards()); // Give rewards
//                                            Bukkit.getServer().getPluginManager().callEvent(new QuestCompleteEvent(quest, profile)); // Fire the quest completed event
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    } else {
//                        sender.sendMessage(ChatColor.RED + "That trigger-id does not exist!");
//                    }
//                } else {
//                    sender.sendMessage(ChatColor.RED + "Bad syntax! /questtrigger player|party <party-member-name|player-name|@p> <trigger-id>");
//                }
//            });
//        } else {
//            sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
//        }
//        return true;
//    }
//
//}
