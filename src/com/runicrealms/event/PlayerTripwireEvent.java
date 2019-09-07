package com.runicrealms.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.IntRange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import com.runicrealms.quests.QuestObjective;
import com.runicrealms.quests.QuestObjectiveType;
import com.runicrealms.task.TaskQueue;
import com.runicrealms.util.RunicCoreHook;

public class PlayerTripwireEvent implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		QuestProfile questProfile = Plugin.getQuestProfile(player.getUniqueId().toString());
		if (event.getAction() == Action.PHYSICAL) {
			if (event.getClickedBlock().getType() == Material.TRIPWIRE ||
					event.getClickedBlock().getType() == Material.TRIPWIRE_HOOK) {
				for (Quest quest : questProfile.getQuests()) {
					if (quest.getQuestState().isCompleted() == false && quest.getQuestState().hasStarted()) {
						for (QuestObjective objective : quest.getObjectives().keySet()) {
							if (objective.getObjectiveNumber() != 1) {
								if (QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 1).isCompleted() == false) {
									continue;
								}
							}
							if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) {
								continue;
							}
							if (objective.getObjectiveType() == QuestObjectiveType.TRIPWIRE) {
								if (new IntRange(objective.getTripwire1().getBlockX(), objective.getTripwire2().getBlockX()).containsInteger(event.getClickedBlock().getX()) &&
										new IntRange(objective.getTripwire1().getBlockY(), objective.getTripwire2().getBlockY()).containsInteger(event.getClickedBlock().getY()) &&
										new IntRange(objective.getTripwire1().getBlockZ(), objective.getTripwire2().getBlockZ()).containsInteger(event.getClickedBlock().getZ())) {
									if (objective.requiresQuestItem()) {
										int aquiredQuestItems = 0;
										for (QuestItem questItem : objective.getQuestItems()) {
											int amount = 0;
											for (ItemStack item : player.getInventory().getContents()) {
												if (item != null) {
													Material material = Material.getMaterial(questItem.getItemType());
													if (item.getType() == material &&
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
												Plugin.removeItem(player.getInventory(), questItem.getItemName(), Material.getMaterial(questItem.getItemType()), questItem.getAmount());
											}
											player.updateInventory();
										}
									}
									objective.setCompleted(true);
									questProfile.save();
									if (objective.hasExecute()) {
										objective.executeCommand(player.getName());
									}
									if (objective.hasCompletedMessage()) {
										for (String message : objective.getCompletedMessage()) {
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
										}
									}
									if (objective.getObjectiveNumber() != QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber()) {
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
										for (String message : QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage()) {
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + message));
										}
									} else {
										quest.getQuestState().setCompleted(true);
										questProfile.save();
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() > 1 ? "s" : "")));
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() > 1 ? "s" : "")));
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience"));
										if (quest.getRewards().hasExecute()) {
											quest.getRewards().executeCommand(player.getName());
										}
										RunicCoreHook.giveRewards(player, quest.getRewards());
										Bukkit.getServer().getPluginManager().callEvent(new QuestCompleteEvent(quest, questProfile));
										if (quest.hasCompletionSpeech()) {
											List<Runnable> runnables = new ArrayList<Runnable>();
											for (String message : quest.getCompletionSpeech()) {
												runnables.add(new Runnable() {
													@Override
													public void run() {
														player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
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
