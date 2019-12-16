package com.runicrealms.runicquests.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.runicrealms.runiccharacters.api.events.CharacterBlockBreakEvent;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.api.QuestCompleteEvent;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.FirstNpcState;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveBreak;
import com.runicrealms.runicquests.task.TaskQueue;
import com.runicrealms.runicquests.util.RunicCoreHook;

public class EventBreakBlock implements Listener {

	@EventHandler
	public void onBreak(CharacterBlockBreakEvent event) {
		Player player = event.getPlayer();
		QuestProfile questProfile = Plugin.getQuestProfile(player.getUniqueId().toString()); // Get player's questing profile
		Map<String, List<Integer>> questCooldowns = Plugin.getQuestCooldowns(); // Get the repeatable quest cooldowns
		for (Quest quest : questProfile.getQuests()) { // Loop through the quests to find a matching objective
			if ((quest.getQuestState().isCompleted() == false && quest.getQuestState().hasStarted()) // Check that the quest is not completed and has been started
					|| (quest.isRepeatable() && quest.getQuestState().isCompleted() && quest.getQuestState().hasStarted())) { // This is a check for repeatable quests
				for (QuestObjective objective : quest.getObjectives()) { // Loop through the quest objectives
					if (objective.isCompleted()) { // Check that the objective is not completed
						continue;
					}
					if (objective.getObjectiveNumber() != 1) { // Check that the previous objective has been completed
						if (QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 1).isCompleted() == false) {
							continue;
						}
					}
					if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) { // Check that the player has accepted the quest
						continue;
					}
					if (objective.getObjectiveType() == QuestObjectiveType.BREAK) { // Check that the objective is of correct type
						QuestObjectiveBreak breakObjective = (QuestObjectiveBreak) objective;
						if (breakObjective.hasBlockAmount()) { // Check if the objective requires you to break multiple blocks
							if (breakObjective.hasBlockLocation()) { // Check if the objective requires that the broken block be in a certain location
								if (event.getBlock().getLocation().getBlockX() != breakObjective.getBlockLocation().getBlockX() ||
										event.getBlock().getLocation().getBlockY() != breakObjective.getBlockLocation().getBlockY() ||
										event.getBlock().getLocation().getBlockZ() != breakObjective.getBlockLocation().getBlockZ() ||
										event.getBlock().getWorld().getName().equalsIgnoreCase(breakObjective.getBlockLocation().getWorld().getName()) == false) { // Check the block location
									continue;
								}
							}
							breakObjective.setBlocksBroken(breakObjective.getBlocksBroken() + 1); // Add to the amount of blocks broken
							if (breakObjective.getBlocksBroken() != breakObjective.getBlockAmount()) { // If the player has not broken the correct amount of blocks, continue
								continue;
							}
						}
						if (breakObjective.getBlockMaterial() == event.getBlock().getType()) { // Check that the block is of correct material
							if (objective.requiresQuestItem()) { // Check if the objective requires a quest item, remove if there is one
								if (Plugin.hasQuestItems(objective, player)) {
									for (QuestItem questItem : objective.getQuestItems()) {
										Plugin.removeItem(player.getInventory(), questItem.getItemName(), questItem.getItemType(), questItem.getAmount());
									}
									player.updateInventory();
								}
							}
							objective.setCompleted(true);
							questProfile.save();
							player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 0); // Play sound
							if (objective.hasExecute()) { // Execute objective commands
								objective.executeCommand(player.getName());
							}
							if (objective.getObjectiveNumber() != QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber()) { // If this is not the last objective
								String goalMessage = QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage(); // Get goal message
								player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 40, 10); // Display goal message title
								if (objective.hasCompletedMessage()) { // If the objective has a completed message
									List<Runnable> runnables = new ArrayList<Runnable>();
									for (String message : objective.getCompletedMessage()) { // Put the completed message into a task queue
										runnables.add(new Runnable() {
											@Override
											public void run() {
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', Plugin.parseMessage(message, player.getName())));
											}
										});
									}
									runnables.add(new Runnable() { // Put the goal message into the task queue
										@Override
										public void run() {
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
										}
									});
									TaskQueue queue = new TaskQueue(runnables);
									queue.startTasks();
								}
							} else { // If we have completed the quest...
								quest.getQuestState().setCompleted(true);
								questProfile.save();
								if (objective.hasCompletedMessage()) { // If there is a completed message
									List<Runnable> runnables = new ArrayList<Runnable>();
									for (String message : objective.getCompletedMessage()) { // Create a task queue with the completed message
										runnables.add(new Runnable() {
											@Override
											public void run() {
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', Plugin.parseMessage(message, player.getName())));
											}
										});
									}
									TaskQueue queue = new TaskQueue(runnables);
									queue.addTasks(new Runnable() { // Add the quest rewards to the task queue
										@Override
										public void run() {
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() == 1 ? "" : "s")));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() == 1 ? "" : "s")));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience Point" + (quest.getRewards().getExperienceReward() == 1 ? "" : "s")));
										}
									});
									queue.startTasks();
								} else { // If there isn't a completed message, display rewards
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() == 1 ? "" : "s")));
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() == 1 ? "" : "s")));
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience Point" + (quest.getRewards().getExperienceReward() == 1 ? "" : "s")));
								}
								if (quest.getRewards().hasExecute()) { // Execute rewards commands
									quest.getRewards().executeCommand(player.getName());
								}
								RunicCoreHook.giveRewards(player, quest.getRewards()); // Give rewards
								if (quest.isRepeatable() == true) { // If the quest is repeatable, setup cooldowns
									questCooldowns.get(player.getUniqueId().toString()).add(quest.getFirstNPC().getId());
									Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), new Runnable() {
										@Override
										public void run() {
											if (questCooldowns.get(player.getUniqueId().toString()).contains(quest.getQuestID())) {
												questCooldowns.get(player.getUniqueId().toString()).remove(quest.getQuestID());
											} else {
												Bukkit.getLogger().log(Level.INFO, "[RunicQuests] ERROR - failed to remove quest cooldown from player \"" + questProfile.getPlayerUUID() + "\"!");
											}
										}
									}, quest.getCooldown() * 20);
								}
								Bukkit.getServer().getPluginManager().callEvent(new QuestCompleteEvent(quest, questProfile)); // Fire the quest completed event
								if (quest.hasCompletionSpeech()) { // This is usless it will be removed
									List<Runnable> runnables = new ArrayList<Runnable>();
									for (String message : quest.getCompletionSpeech()) {
										runnables.add(new Runnable() {
											@Override
											public void run() {
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', Plugin.parseMessage(message, player.getName())));
											}
										});
									}
									TaskQueue secondQueue = new TaskQueue(runnables);
									secondQueue.startTasks();
								}
							}
						}
					}
				}
			}
		}
	}

}
