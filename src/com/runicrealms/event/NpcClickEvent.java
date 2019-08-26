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
import com.runicrealms.quests.QuestObjective;
import com.runicrealms.quests.QuestObjectiveType;
import com.runicrealms.quests.SpeechState;
import com.runicrealms.task.TaskQueue;

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
					TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getFirstNPC().getQuestCompletedSpeech(), quest.getFirstNPC().getNpcName()));
					queue.setCompletedTask(new Runnable() {
						@Override
						public void run() {
							quest.getFirstNPC().setSpeechState(SpeechState.COMPLETED);
							npcs.remove(quest.getFirstNPC().getId());
						}
					});
					queue.startTasks();
					quest.getFirstNPC().setSpeechState(SpeechState.STARTED);
					npcs.put(quest.getFirstNPC().getId(), queue);
				} else {
					for (QuestObjective objective : quest.getObjectives().keySet()) {
						if (objective.getObjectiveType() == QuestObjectiveType.TALK) {
							if (objective.getQuestNpc().getCitizensNpc().getId() == event.getNPC().getId()) {
								TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, objective.getQuestNpc().getQuestCompletedSpeech(), objective.getQuestNpc().getNpcName()));
								queue.setCompletedTask(new Runnable() {
									@Override
									public void run() {
										objective.getQuestNpc().setSpeechState(SpeechState.COMPLETED);
										npcs.remove(objective.getQuestNpc().getId());
									}
								});
								queue.startTasks();
								objective.getQuestNpc().setSpeechState(SpeechState.STARTED);
								npcs.put(objective.getQuestNpc().getId(), queue);
							}
						}
					}
				}
			}
			if ((quest.getQuestState().hasStarted() == false && quest.getQuestState().isCompleted() == false) ||
					(quest.isRepeatable() && quest.getQuestState().hasStarted() && quest.getQuestState().isCompleted())) {
				if (quest.getFirstNPC().getCitizensNpc().getId() == event.getNPC().getId()) {
					if (QuestObjective.getObjective(quest.getObjectives(), 1).isCompleted() == false) {
						if (quest.getFirstNPC().getSpeechState() == SpeechState.NOT_STARTED) {
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
										quest.getFirstNPC().setSpeechState(SpeechState.COMPLETED);
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
								quest.getFirstNPC().setSpeechState(SpeechState.STARTED);
								npcs.put(quest.getFirstNPC().getId(), queue);
							} else {
								if (quest.getFirstNPC().getState() == FirstNpcState.PENDING || quest.getFirstNPC().getState() == FirstNpcState.DENIED) {
									quest.getQuestState().setStarted(true);
									questProfile.save();
									if (quest.getFirstNPC().hasExecute()) {
										quest.getFirstNPC().executeCommand(player.getName());
									}
									quest.getFirstNPC().setSpeechState(SpeechState.COMPLETED);
									quest.getFirstNPC().setState(FirstNpcState.ACCEPTED);
									TaskQueue queue = new TaskQueue(new Runnable() {
										@Override
										public void run()  {
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[1/1] &e" + quest.getFirstNPC().getNpcName() + ": &6" + quest.getFirstNPC().getAcceptedMessage()));
										}
									}, new Runnable() {
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
											quest.getFirstNPC().setSpeechState(SpeechState.COMPLETED);
											quest.getFirstNPC().setState(FirstNpcState.PENDING);
											npcs.remove(quest.getFirstNPC().getId());
										}
									});
									queue.startTasks();
									quest.getFirstNPC().setSpeechState(SpeechState.STARTED);
									npcs.put(quest.getFirstNPC().getId(), queue);
								}
							}
						} else if (quest.getFirstNPC().getSpeechState() == SpeechState.STARTED){
							npcs.get(quest.getFirstNPC().getId()).nextTask();
						} else if (quest.getFirstNPC().getSpeechState() == SpeechState.COMPLETED) {
							if (quest.getFirstNPC().getState() == FirstNpcState.PENDING || quest.getFirstNPC().getState() == FirstNpcState.DENIED) {
								quest.getQuestState().setStarted(true);
								questProfile.save();
								if (quest.getFirstNPC().hasExecute()) {
									quest.getFirstNPC().executeCommand(player.getName());
								}
								quest.getFirstNPC().setSpeechState(SpeechState.COMPLETED);
								quest.getFirstNPC().setState(FirstNpcState.ACCEPTED);
								TaskQueue queue = new TaskQueue(new Runnable() {
									@Override
									public void run()  {
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[1/1] &e" + quest.getFirstNPC().getNpcName() + ": &6" + quest.getFirstNPC().getAcceptedMessage()));
									}
								}, new Runnable() {
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
							}
						}
					}
				}
			} else if (quest.getQuestState().hasStarted()) {
				for (QuestObjective objective : quest.getObjectives().keySet()) {
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
							boolean hasQuestItem = false;
							for (ItemStack item : player.getInventory().getContents()) {
								if (item != null) {
									if (item.getType() == Material.getMaterial(objective.getQuestItem().getItemType())) {
										if (ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase(objective.getQuestItem().getItemName())) {
											player.getInventory().remove(item.asQuantity(1));
											hasQuestItem = true;
											break;
										}
									}
								}
							}
							if (!hasQuestItem) {
								TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, objective.getQuestNpc().getIdleSpeech(), objective.getQuestNpc().getNpcName()));
								queue.setCompletedTask(new Runnable() {
									@Override
									public void run() {
										objective.getQuestNpc().setSpeechState(SpeechState.NOT_STARTED);
										npcs.remove(objective.getQuestNpc().getId());
									}
								});
								queue.startTasks();
								objective.getQuestNpc().setSpeechState(SpeechState.STARTED);
								npcs.put(objective.getQuestNpc().getId(), queue);
								continue;
							}
						}
						if (objective.getObjectiveType() == QuestObjectiveType.TALK) {
							if (objective.getQuestNpc().getCitizensNpc().getId() == event.getNPC().getId()) {
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
										objective.getQuestNpc().setSpeechState(SpeechState.COMPLETED);
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
										}
									});
								}
								queue.startTasks();
								objective.getQuestNpc().setSpeechState(SpeechState.STARTED);
								npcs.put(objective.getQuestNpc().getId(), queue);
							}
						}
					} else {
						if (objective.getObjectiveType() == QuestObjectiveType.TALK) {
							if (objective.getQuestNpc().getCitizensNpc().getId() == event.getNPC().getId()) {
								if (objective.getQuestNpc().getSpeechState() == SpeechState.STARTED) {
									npcs.get(objective.getQuestNpc().getId()).nextTask();
								}
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
						quest.getFirstNPC().setSpeechState(SpeechState.NOT_STARTED);
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[1/1] &e" + quest.getFirstNPC().getNpcName() + ": &6" + quest.getFirstNPC().getDeniedMessage()));
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
