package com.runicrealms.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.runicrealms.Plugin;
import com.runicrealms.api.QuestCompleteEvent;
import com.runicrealms.player.QuestProfile;
import com.runicrealms.quests.FirstNpcState;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestItem;
import com.runicrealms.quests.QuestObjective;
import com.runicrealms.quests.QuestObjectiveType;
import com.runicrealms.task.TaskQueue;
import com.runicrealms.util.RunicCoreHook;

public class PlayerBreakBlockEvent implements Listener {

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		QuestProfile questProfile = Plugin.getQuestProfile(player.getUniqueId().toString());
		Map<String, List<Integer>> questCooldowns = Plugin.getQuestCooldowns();
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
					if (objective.getObjectiveType() == QuestObjectiveType.BREAK) {
						if (objective.hasBlockAmount()) {
							if (objective.hasBlockLocation()) {
								if (event.getBlock().getLocation().getBlockX() != objective.getBlockLocation().getBlockX() ||
										event.getBlock().getLocation().getBlockY() != objective.getBlockLocation().getBlockY() ||
										event.getBlock().getLocation().getBlockZ() != objective.getBlockLocation().getBlockZ()) {
									continue;
								}
							}
							objective.setBlocksBroken(objective.getBlocksBroken() + 1);
							if (objective.getBlocksBroken() != objective.getBlockAmount()) {
								continue;
							}
						}
						if (objective.getBlockMaterial() == event.getBlock().getType()) {
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
							if (objective.hasExecute()) {
								objective.executeCommand(player.getName());
							}
							if (objective.getObjectiveNumber() != QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber()) {
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
											for (String message : QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage()) {
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + message));
											}
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
