package com.runicrealms.runicquests.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.runicrealms.runiccharacters.api.events.CharacterMythicMobDeathEvent;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.api.QuestCompleteEvent;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.FirstNpcState;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveSlay;
import com.runicrealms.runicquests.task.TaskQueue;
import com.runicrealms.runicquests.util.RunicCoreHook;

public class EventKillMythicMob implements Listener {

	@EventHandler
	public void onKill(CharacterMythicMobDeathEvent event) {
		if (event.getKiller() instanceof Player) {
			Player player = (Player) event.getKiller();
			QuestProfile questProfile = Plugin.getQuestProfile(player.getUniqueId().toString()); // Get player questing profile
			Map<UUID, Map<Integer, List<Integer>>> questCooldowns = Plugin.getQuestCooldowns();
			for (Quest quest : questProfile.getQuests()) { // Loop through quest to find a matching objective to the mob killed
				if ((quest.getQuestState().isCompleted() == false && quest.getQuestState().hasStarted())
						|| (quest.isRepeatable() && quest.getQuestState().isCompleted() && quest.getQuestState().hasStarted())) { // Checks if the quest is "active"
					for (QuestObjective objective : quest.getObjectives()) { // Loops through the objectives to find a match
						if (objective.isCompleted()) { // Checks to see that the objective is not completed
							continue;
						}
						if (objective.getObjectiveNumber() != 1) { // Checks that the previous objective is completed (so this objective is the current one)
							if (QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 1).isCompleted() == false) {
								continue;
							}
						}
						if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) { // Check that the player has accepted the quest
							continue;
						}
						if (objective.getObjectiveType() == QuestObjectiveType.SLAY) { // Checks that the objective is of type slay
							QuestObjectiveSlay slayObjective = (QuestObjectiveSlay) objective;
							for (String mob : slayObjective.getMobNames()) { // Checks that the mob in the objective has the correct name
								if (event.getMob().getType().getInternalName().equalsIgnoreCase(mob)) {
									slayObjective.setMobsKilled(slayObjective.getMobsKilled() + 1); // Add to the slayed mobs
									if (slayObjective.getMobsKilled() == slayObjective.getMobAmount()) { // Check if player has killed required amount
										if (objective.requiresQuestItem()) { // Check for quest item
											if (Plugin.hasQuestItems(objective, player)) {
												for (QuestItem questItem : objective.getQuestItems()) {
													Plugin.removeItem(player.getInventory(), questItem.getItemName(), questItem.getItemType(), questItem.getAmount());
												}
												player.updateInventory();
											}
										}
										objective.setCompleted(true);
										questProfile.save();
										if (objective.hasExecute()) { // Executes the objective commands
											objective.executeCommand(player.getName());
										}
										player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 0); // Play a sound
										if (objective.getObjectiveNumber() != QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber()) { // Check to see if we have not finished the quest
											String goalMessage = QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage(); // Get the goal message
											player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 40, 10);  // Display a title on the screen
											if (objective.hasCompletedMessage()) { // If objective has a completed message, create a task queue and add message + new objective message to it
												List<Runnable> runnables = new ArrayList<Runnable>();
												for (String message : objective.getCompletedMessage()) {
													runnables.add(new Runnable() {
														@Override
														public void run() {
															player.sendMessage(ChatColor.translateAlternateColorCodes('&', Plugin.parseMessage(message, player.getName())));
														}
													});
												}
												runnables.add(new Runnable() {
													@Override
													public void run() {
														player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
														player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
													}
												});
												TaskQueue queue = new TaskQueue(runnables);
												queue.startTasks();
											}
										} else { // If we have completed the quest
											quest.getQuestState().setCompleted(true);
											questProfile.save();
											if (objective.hasCompletedMessage()) { // If we have a completed message, create a task queue and add completed message, and rewards to queue
												List<Runnable> runnables = new ArrayList<Runnable>();
												for (String message : objective.getCompletedMessage()) { 
													runnables.add(new Runnable() {
														@Override
														public void run() {
															player.sendMessage(ChatColor.translateAlternateColorCodes('&', Plugin.parseMessage(message, player.getName())));
														}
													});
												}
												TaskQueue queue = new TaskQueue(runnables);
												queue.addTasks(new Runnable() {
													@Override
													public void run() {
														player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
														player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() == 1 ? "" : "s")));
														player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() == 1 ? "" : "s")));
														player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience Point" + (quest.getRewards().getExperienceReward() == 1 ? "" : "s")));
													}
												});
												queue.startTasks();
											} else { // If we don't have a completed message, display rewards
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() == 1 ? "" : "s")));
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() == 1 ? "" : "s")));
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience Point" + (quest.getRewards().getExperienceReward() == 1 ? "" : "s")));
											}
											if (quest.getRewards().hasExecute()) { // Execute quest commands
												quest.getRewards().executeCommand(player.getName());
											}
											RunicCoreHook.giveRewards(player, quest.getRewards()); // Give rewards
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
											Bukkit.getServer().getPluginManager().callEvent(new QuestCompleteEvent(quest, questProfile)); // Call the quest event
										}
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