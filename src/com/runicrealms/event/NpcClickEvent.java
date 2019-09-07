package com.runicrealms.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.runicrealms.Plugin;
import com.runicrealms.api.QuestAcceptEvent;
import com.runicrealms.api.QuestCompleteEvent;
import com.runicrealms.api.QuestDenyEvent;
import com.runicrealms.player.QuestProfile;
import com.runicrealms.quests.FirstNpcState;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestItem;
import com.runicrealms.quests.QuestObjective;
import com.runicrealms.quests.QuestObjectiveType;
import com.runicrealms.task.TaskQueue;
import com.runicrealms.util.RunicCoreHook;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class NpcClickEvent implements Listener {

	private static HashMap<Integer, TaskQueue> npcs = new HashMap<Integer, TaskQueue>();

	@EventHandler
	public void onNpcRightClick(NPCRightClickEvent event) { // TODO check for requirements
		Player player = event.getClicker();
		QuestProfile questProfile = Plugin.getQuestProfile(player.getUniqueId().toString());
		for (Quest quest : questProfile.getQuests()) {
			if (quest.getQuestState().isCompleted()) {
				if (quest.getFirstNPC().getCitizensNpc().getId() == event.getNPC().getId()) {
					if (quest.getFirstNPC().hasQuestCompletedSpeech()) {
						TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getFirstNPC().getQuestCompletedSpeech(), quest.getFirstNPC().getNpcName()));
						queue.setCompletedTask(new Runnable() {
							@Override
							public void run() {
								npcs.remove(quest.getFirstNPC().getId());
							}
						});
						queue.startTasks();
						npcs.put(quest.getFirstNPC().getId(), queue);
					}
				} else {
					for (QuestObjective objective : quest.getObjectives().keySet()) {
						if (objective.getObjectiveType() == QuestObjectiveType.TALK) {
							if (objective.getQuestNpc().getCitizensNpc().getId() == event.getNPC().getId()) {
								if (objective.getQuestNpc().hasQuestCompletedSpeech()) {
									TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, objective.getQuestNpc().getQuestCompletedSpeech(), objective.getQuestNpc().getNpcName()));
									queue.setCompletedTask(new Runnable() {
										@Override
										public void run() {
											npcs.remove(objective.getQuestNpc().getId());
										}
									});
									queue.startTasks();
									npcs.put(objective.getQuestNpc().getId(), queue);
								}
							}
						}
					}
				}
			}
			if ((quest.getQuestState().hasStarted() == false && quest.getQuestState().isCompleted() == false) ||
					(quest.isRepeatable() && quest.getQuestState().hasStarted() && quest.getQuestState().isCompleted())) {
				if (quest.getFirstNPC().getCitizensNpc().getId() == event.getNPC().getId()) {
					if (QuestObjective.getObjective(quest.getObjectives(), 1).isCompleted() == false) {
						if (!npcs.containsKey(quest.getFirstNPC().getId())) {
							if (!quest.getFirstNPC().isDeniable()) {
								quest.getQuestState().setStarted(true);
								questProfile.save();
								if (quest.getFirstNPC().hasExecute()) {
									quest.getFirstNPC().executeCommand(player.getName());
								}
								TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getFirstNPC().getSpeech(), quest.getFirstNPC().getNpcName()));
								queue.setCompletedTask(new Runnable() {
									@Override
									public void run() {
										npcs.remove(quest.getFirstNPC().getId());
									}
								});
								queue.addTasks(new Runnable() {
									@Override
									public void run() {
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
										for (String message : QuestObjective.getObjective(quest.getObjectives(), 1).getGoalMessage()) {
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + message));
										}
									}
								});
								queue.startTasks();
								npcs.put(quest.getFirstNPC().getId(), queue);
							} else {
								if (quest.getFirstNPC().getState() == FirstNpcState.PENDING || quest.getFirstNPC().getState() == FirstNpcState.DENIED) {
									quest.getQuestState().setStarted(true);
									questProfile.save();
									if (quest.getFirstNPC().hasExecute()) {
										quest.getFirstNPC().executeCommand(player.getName());
									}
									quest.getFirstNPC().setState(FirstNpcState.ACCEPTED);
									questProfile.save();
									TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getFirstNPC().getAcceptedMessage(), quest.getFirstNPC().getNpcName()));
									queue.addTasks(new Runnable() {
										@Override
										public void run() {
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
											for (String message : QuestObjective.getObjective(quest.getObjectives(), 1).getGoalMessage()) {
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + message));
											}
										}
									});
									queue.startTasks();
									Bukkit.getServer().getPluginManager().callEvent(new QuestAcceptEvent(quest, questProfile));
								} else if (quest.getFirstNPC().getState() == FirstNpcState.NEUTRAL) {
									boolean meetsRequirements = true;
									if (!RunicCoreHook.isRequiredLevel(player, quest.getRequirements().getLevelRequirement())) {
										meetsRequirements = false;
										TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getRequirements().getLevelNotMetMsg(), quest.getFirstNPC().getNpcName()));
										queue.setCompletedTask(new Runnable() {
											@Override
											public void run() {
												npcs.remove(quest.getFirstNPC().getId());
											}
										});
										queue.startTasks();
										npcs.put(quest.getFirstNPC().getId(), queue);
									} else if (quest.getRequirements().hasCraftingRequirement()) {
										if (!RunicCoreHook.isRequiredCraftingLevel(player, quest.getRequirements().getCraftingProfessionType(), quest.getRequirements().getCraftingRequirement())) {
											meetsRequirements = false;
										}
									} else if (quest.getRequirements().hasCompletedQuestRequirement()) {
										if (!RunicCoreHook.hasCompletedRequiredQuests(player, quest.getRequirements().getCompletedQuestsRequirement())) {
											meetsRequirements = false;
										}
									}
									if (meetsRequirements) {
										List<Runnable> runnables = makeSpeechRunnables(player, quest.getFirstNPC().getSpeech(), quest.getFirstNPC().getNpcName());
										runnables.add(new Runnable() {
											@Override
											public void run() {
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Left click to deny quest, right click to accept"));
											}
										});
										TaskQueue queue = new TaskQueue(runnables);
										queue.setCompletedTask(new Runnable() {
											@Override
											public void run() {
												quest.getFirstNPC().setState(FirstNpcState.PENDING);
												questProfile.save();
												npcs.remove(quest.getFirstNPC().getId());
											}
										});
										queue.startTasks();
										npcs.put(quest.getFirstNPC().getId(), queue);
									}
								}
							}
						} else {
							npcs.get(quest.getFirstNPC().getId()).nextTask();
						}
					}
				}
			} else if (quest.getQuestState().hasStarted()) {
				for (QuestObjective objective : quest.getObjectives().keySet()) {
					if (objective.getObjectiveType() == QuestObjectiveType.TALK) {
						if (objective.getQuestNpc().getCitizensNpc().getId() == event.getNPC().getId()) {
							if (npcs.containsKey(objective.getQuestNpc().getId())) {
								npcs.get(objective.getQuestNpc().getId()).nextTask();
								return;
							}
							if (objective.isCompleted() == false) {
								if (objective.getObjectiveNumber() != 1) {
									if (QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 1).isCompleted() == false) {
										continue;
									}
								}
								if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) {
									continue;
								}
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
										TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, objective.getQuestNpc().getIdleSpeech(), objective.getQuestNpc().getNpcName()));
										queue.setCompletedTask(new Runnable() {
											@Override
											public void run() {
												npcs.remove(objective.getQuestNpc().getId());
											}
										});
										queue.startTasks();
										npcs.put(objective.getQuestNpc().getId(), queue);
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
								TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, objective.getQuestNpc().getSpeech(), objective.getQuestNpc().getNpcName()));
								queue.setCompletedTask(new Runnable() {
									@Override
									public void run() {
										npcs.remove(objective.getQuestNpc().getId());
									}
								});
								if (objective.getObjectiveNumber() != QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber()) {
									queue.addTasks(new Runnable() {
										@Override
										public void run() {
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
											for (String message : QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage()) {
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + message));
											}
										}
									});
								} else {
									queue.addTasks(new Runnable() {
										@Override
										public void run() {
											quest.getQuestState().setCompleted(true);
											questProfile.save();
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() > 1 ? "s" : "")));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() > 1 ? "s" : "")));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience"));
											if (quest.getRewards().hasExecute()) {
												quest.getRewards().executeCommand(player.getName());
											}
											Bukkit.getServer().getPluginManager().callEvent(new QuestCompleteEvent(quest, questProfile));
											if (quest.hasCompletionSpeech()) {
												if (quest.useLastNpcNameForCompletionSpeech()) {
													TaskQueue secondQueue = new TaskQueue(makeSpeechRunnables(player, quest.getCompletionSpeech(), objective.getQuestNpc().getNpcName()));
													secondQueue.startTasks();
												} else {
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
									});
								}
								queue.startTasks();
								npcs.put(objective.getQuestNpc().getId(), queue);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onNpcLeftClick(NPCLeftClickEvent event) {
		Player player = event.getClicker();
		QuestProfile questProfile = Plugin.getQuestProfile(event.getClicker().getUniqueId().toString());
		for (Quest quest : questProfile.getQuests()) {
			if (quest.getQuestState().hasStarted() == false && quest.getQuestState().isCompleted() == false) {
				if (quest.getFirstNPC().getCitizensNpc().getId() == event.getNPC().getId()) {
					if (QuestObjective.getObjective(quest.getObjectives(), 1).isCompleted() == false) {
						quest.getFirstNPC().setState(FirstNpcState.DENIED);
						questProfile.save();
						TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getFirstNPC().getDeniedMessage(), quest.getFirstNPC().getNpcName()));
						queue.startTasks();
						Bukkit.getServer().getPluginManager().callEvent(new QuestDenyEvent(quest, questProfile));
					}
				}
			}
		}
	}

	private static List<Runnable> makeSpeechRunnables(Player player, List<String> messages, String name) {
		List<Runnable> runnables = new ArrayList<Runnable>();
		for (String message : messages) {
			runnables.add(new Runnable() {
				@Override
				public void run() {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[" + (messages.indexOf(message) + 1) + "/" + messages.size() + "] &e" + name + ": &6" + message));
				}
			});
		}
		return runnables;
	}

}
