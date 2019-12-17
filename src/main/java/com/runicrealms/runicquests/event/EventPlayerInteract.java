package com.runicrealms.runicquests.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.math.IntRange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

import com.runicrealms.runiccharacters.api.events.CharacterPlayerInteractEvent;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.api.QuestCompleteEvent;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.FirstNpcState;
import com.runicrealms.runicquests.quests.ObjectiveTripwire;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTripwire;
import com.runicrealms.runicquests.task.TaskQueue;
import com.runicrealms.runicquests.util.RunicCoreHook;

public class EventPlayerInteract implements Listener {

	@EventHandler
	public void onPlayerInteract(CharacterPlayerInteractEvent event) {
		Player player = event.getPlayer();
		QuestProfile questProfile = Plugin.getQuestProfile(player.getUniqueId().toString()); // Get the questing profile
		Map<UUID, Map<Integer, List<Integer>>> questCooldowns = Plugin.getQuestCooldowns();
		if (event.getAction() == Action.PHYSICAL) { // Check that the player has interacted with a physical block
			if (event.getClickedBlock().getType() == Material.TRIPWIRE ||
					event.getClickedBlock().getType() == Material.TRIPWIRE_HOOK) { // Check that the block is a tripwire
				for (Quest quest : questProfile.getQuests()) { // Loop through the quests
					if ((quest.getQuestState().isCompleted() == false && quest.getQuestState().hasStarted()) // Check that the quest has been started and is not completed
							|| (quest.isRepeatable() && quest.getQuestState().isCompleted() && quest.getQuestState().hasStarted())) { // Special check for repeatable quests
						for (QuestObjective objective : quest.getObjectives()) { // Loop through objectives
							if (objective.isCompleted()) { // Check that the objective has not been completed
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
							if (objective.getObjectiveType() == QuestObjectiveType.TRIPWIRE) { // Check the objective type
								QuestObjectiveTripwire tripwireObjective = (QuestObjectiveTripwire) objective;
								for (ObjectiveTripwire tripwire : tripwireObjective.getTripwires()) { // Loop through the possible tripwires
									if (new IntRange(tripwire.getCorner1().getBlockX(), tripwire.getCorner2().getBlockX()).containsInteger(event.getClickedBlock().getX()) &&
											new IntRange(tripwire.getCorner1().getBlockY(), tripwire.getCorner2().getBlockY()).containsInteger(event.getClickedBlock().getY()) &&
											new IntRange(tripwire.getCorner1().getBlockZ(), tripwire.getCorner2().getBlockZ()).containsInteger(event.getClickedBlock().getZ()) &&
											event.getClickedBlock().getWorld().getName().equalsIgnoreCase(tripwire.getCorner1().getWorld().getName())) { // Check the location of the tripwires
										if (objective.requiresQuestItem()) { // Check for a quest item, remove it from inventory
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
										if (objective.getObjectiveNumber() != QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber()) { // Check that we haven't completed the quest
											String goalMessage = QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage(); // Get the goal message
											player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 40, 10); // Send a goal message title
											if (objective.hasCompletedMessage()) { // Check for a completed message
												List<Runnable> runnables = new ArrayList<Runnable>();
												for (String message : objective.getCompletedMessage()) { // Create a task queue with the completed message
													runnables.add(new Runnable() {
														@Override
														public void run() {
															player.sendMessage(ChatColor.translateAlternateColorCodes('&', Plugin.parseMessage(message, player.getName())));
														}
													});
												}
												runnables.add(new Runnable() { // Add the new objective message to the task queue
													@Override
													public void run() {
														player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
														player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
													}
												});
												TaskQueue queue = new TaskQueue(runnables);
												queue.startTasks();
											}
										} else { // If we have finished the quest
											quest.getQuestState().setCompleted(true);
											questProfile.save();
											if (objective.hasCompletedMessage()) { // If we have a completed message
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
											} else { // If we don't have a completed message, display the rewards
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() == 1 ? "" : "s")));
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() == 1 ? "" : "s")));
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience Point" + (quest.getRewards().getExperienceReward() == 1 ? "" : "s")));
											}
											if (quest.getRewards().hasExecute()) { // Execute the rewards commands
												quest.getRewards().executeCommand(player.getName());
											}
											RunicCoreHook.giveRewards(player, quest.getRewards()); // Give the rewards
											if (quest.isRepeatable() == true) { // If the quest is repeatable, setup cooldown
												questCooldowns.get(player.getUniqueId()).get(event.getCharacter().getSlot()).add(quest.getQuestID());
												Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), new Runnable() {
													@Override
													public void run() {
														if (questCooldowns.get(player.getUniqueId()).get(event.getCharacter().getSlot()).contains(quest.getQuestID())) {
															questCooldowns.get(player.getUniqueId()).get(event.getCharacter().getSlot()).remove(quest.getQuestID());
														} else {
															Bukkit.getLogger().log(Level.INFO, "[RunicQuests] ERROR - failed to remove quest cooldown from player \"" + questProfile.getPlayerUUID() + "\"!");
														}
													}
												}, quest.getCooldown() * 20);
											}
											Bukkit.getServer().getPluginManager().callEvent(new QuestCompleteEvent(quest, questProfile)); // Fire the quest completed event
										}
										break;
									}
								}
							}
						}
					}
				}
			}
		}
	}

}
