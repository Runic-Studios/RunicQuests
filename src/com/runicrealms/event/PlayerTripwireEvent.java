package com.runicrealms.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.runicrealms.Plugin;
import com.runicrealms.api.QuestCompleteEvent;
import com.runicrealms.player.QuestProfile;
import com.runicrealms.quests.FirstNpcState;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestItem;
import com.runicrealms.quests.QuestObjectiveType;
import com.runicrealms.quests.objective.QuestObjective;
import com.runicrealms.quests.objective.QuestObjectiveTripwire;
import com.runicrealms.task.TaskQueue;
import com.runicrealms.util.RunicCoreHook;

public class PlayerTripwireEvent implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		QuestProfile questProfile = Plugin.getQuestProfile(player.getUniqueId().toString());
		Map<String, List<Integer>> questCooldowns = Plugin.getQuestCooldowns();
		if (event.getAction() == Action.PHYSICAL) {
			if (event.getClickedBlock().getType() == Material.TRIPWIRE ||
					event.getClickedBlock().getType() == Material.TRIPWIRE_HOOK) {
				for (Quest quest : questProfile.getQuests()) {
					if ((quest.getQuestState().isCompleted() == false && quest.getQuestState().hasStarted())
							|| (quest.isRepeatable() && quest.getQuestState().isCompleted() && quest.getQuestState().hasStarted())) {
						for (QuestObjective objective : quest.getObjectives()) {
							if (objective.isCompleted()) {
								continue;
							}
							if (objective.getObjectiveNumber() != 1) {
								if (QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 1).isCompleted() == false) {
									continue;
								}
							}
							if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) {
								continue;
							}
							if (objective.getObjectiveType() == QuestObjectiveType.TRIPWIRE) {
								QuestObjectiveTripwire tripwireObjective = (QuestObjectiveTripwire) objective;
								if (new IntRange(tripwireObjective.getTripwire1().getBlockX(), tripwireObjective.getTripwire2().getBlockX()).containsInteger(event.getClickedBlock().getX()) &&
										new IntRange(tripwireObjective.getTripwire1().getBlockY(), tripwireObjective.getTripwire2().getBlockY()).containsInteger(event.getClickedBlock().getY()) &&
										new IntRange(tripwireObjective.getTripwire1().getBlockZ(), tripwireObjective.getTripwire2().getBlockZ()).containsInteger(event.getClickedBlock().getZ()) &&
										event.getClickedBlock().getWorld().toString().equalsIgnoreCase(Plugin.WORLD_NAME)) {
									if (objective.requiresQuestItem()) {
										int aquiredQuestItems = 0;
										for (QuestItem questItem : objective.getQuestItems()) {
											int amount = 0;
											for (ItemStack item : player.getInventory().getContents()) {
												if (item != null) {
													if (item.getType().name().equalsIgnoreCase(questItem.getItemType()) &&
															ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase(questItem.getItemName())) {
														amount += item.getAmount();
														if (amount >= questItem.getAmount()) {
															aquiredQuestItems++;
															break;
														}
													}
												}
											}
										}
										if (aquiredQuestItems != objective.getQuestItems().size()) { 
											continue;
										} else {
											for (QuestItem questItem : objective.getQuestItems()) {
												Plugin.removeItem(player.getInventory(), questItem.getItemName(), questItem.getItemType(), questItem.getAmount());
											}
											player.updateInventory();
										}
									}
									objective.setCompleted(true);
									questProfile.save();
									player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 0);
									if (objective.hasExecute()) {
										objective.executeCommand(player.getName());
									}
									if (objective.getObjectiveNumber() != QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber()) {
										String goalMessage = QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage();
										player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 40, 10);
										if (objective.hasCompletedMessage()) {
											List<Runnable> runnables = new ArrayList<Runnable>();
											for (String message : objective.getCompletedMessage()) {
												runnables.add(new Runnable() {
													@Override
													public void run() {
														player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replaceAll("%player%", player.getName())));
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
									} else {
										quest.getQuestState().setCompleted(true);
										questProfile.save();
										if (objective.hasCompletedMessage()) {
											List<Runnable> runnables = new ArrayList<Runnable>();
											for (String message : objective.getCompletedMessage()) {
												runnables.add(new Runnable() {
													@Override
													public void run() {
														player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replaceAll("%player%", player.getName())));
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
										} else {
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() == 1 ? "" : "s")));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() == 1 ? "" : "s")));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience Point" + (quest.getRewards().getExperienceReward() == 1 ? "" : "s")));
										}
										if (quest.getRewards().hasExecute()) {
											quest.getRewards().executeCommand(player.getName());
										}
										RunicCoreHook.giveRewards(player, quest.getRewards());
										if (quest.isRepeatable() == true) {
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
										Bukkit.getServer().getPluginManager().callEvent(new QuestCompleteEvent(quest, questProfile));
										if (quest.hasCompletionSpeech()) {
											List<Runnable> runnables = new ArrayList<Runnable>();
											for (String message : quest.getCompletionSpeech()) {
												runnables.add(new Runnable() {
													@Override
													public void run() {
														player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replaceAll("%player%", player.getName())));
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
	}

}
