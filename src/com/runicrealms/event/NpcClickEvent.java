package com.runicrealms.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.runicrealms.Plugin;
import com.runicrealms.api.QuestAcceptEvent;
import com.runicrealms.api.QuestCompleteEvent;
import com.runicrealms.api.QuestDenyEvent;
import com.runicrealms.player.QuestProfile;
import com.runicrealms.quests.FirstNpcState;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestIdleMessage;
import com.runicrealms.quests.QuestItem;
import com.runicrealms.quests.QuestObjectiveType;
import com.runicrealms.quests.objective.QuestObjective;
import com.runicrealms.quests.objective.QuestObjectiveBreak;
import com.runicrealms.quests.objective.QuestObjectiveSlay;
import com.runicrealms.quests.objective.QuestObjectiveTalk;
import com.runicrealms.task.TaskQueue;
import com.runicrealms.util.RunicCoreHook;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class NpcClickEvent implements Listener {

	@EventHandler
	public void onNpcRightClick(NPCRightClickEvent event) {
		Player player = event.getClicker();
		QuestProfile questProfile = Plugin.getQuestProfile(player.getUniqueId().toString());
		HashMap<Integer, TaskQueue> npcs = Plugin.getNpcTaskQueues();
		Map<String, List<Integer>> questCooldowns = Plugin.getQuestCooldowns();
		for (Quest quest : questProfile.getQuests()) {
			if (quest.getQuestState().isCompleted() && quest.isRepeatable() == false) {
				if (quest.getFirstNPC().getCitizensNpc().getId() == event.getNPC().getId()) {
					if (quest.getFirstNPC().hasQuestCompletedSpeech()) {
						TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getFirstNPC().getQuestCompletedSpeech(), quest.getFirstNPC().getNpcName()));
						queue.setCompletedTask(new Runnable() {
							@Override
							public void run() {
								npcs.remove(quest.getFirstNPC().getId());
							}
						});
						npcs.put(quest.getFirstNPC().getId(), queue);
						queue.startTasks();
						continue;
					}
				}
			}
			if ((quest.getQuestState().isCompleted() == false) ||
					(quest.isRepeatable() && quest.getQuestState().hasStarted() && quest.getQuestState().isCompleted())) {
				if (quest.getFirstNPC().getCitizensNpc().getId() == event.getNPC().getId() &&
						questCooldowns.get(player.getUniqueId().toString()).contains(quest.getQuestID()) == false) {
					if (QuestObjective.getObjective(quest.getObjectives(), 1).isCompleted() == false || quest.isRepeatable()) {
						if (!npcs.containsKey(quest.getFirstNPC().getId())) {
							if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED || (quest.isRepeatable() && Plugin.allObjectivesComplete(quest))) {
								if (quest.isRepeatable()) {
									for (QuestObjective qobjective : quest.getObjectives()) {
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
											quest.getFirstNPC().setState(FirstNpcState.ACCEPTED);
											questProfile.save();
										}
									});
									queue.addTasks(new Runnable() {
										@Override
										public void run() {
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + QuestObjective.getObjective(quest.getObjectives(), 1).getGoalMessage()));
										}
									});
									npcs.put(quest.getFirstNPC().getId(), queue);
									queue.startTasks();
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
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + QuestObjective.getObjective(quest.getObjectives(), 1).getGoalMessage()));
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
											npcs.put(quest.getFirstNPC().getId(), queue);
											queue.startTasks();
										} else if (quest.getRequirements().hasCraftingRequirement()) {
											if (!RunicCoreHook.isRequiredCraftingLevel(player, quest.getRequirements().getCraftingProfessionType(), quest.getRequirements().getCraftingRequirement())) {
												meetsRequirements = false;
												TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getRequirements().getCraftingLevelNotMetMsg(), quest.getFirstNPC().getNpcName()));
												queue.setCompletedTask(new Runnable() {
													@Override
													public void run() {
														npcs.remove(quest.getFirstNPC().getId());
													}
												});
												npcs.put(quest.getFirstNPC().getId(), queue);
												queue.startTasks();

											}
										} else if (quest.getRequirements().hasCompletedQuestRequirement()) {
											if (!RunicCoreHook.hasCompletedRequiredQuests(player, quest.getRequirements().getCompletedQuestsRequirement())) {
												meetsRequirements = false;
												TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getRequirements().getCompletedQuestsNotMetMsg(), quest.getFirstNPC().getNpcName()));
												queue.setCompletedTask(new Runnable() {
													@Override
													public void run() {
														npcs.remove(quest.getFirstNPC().getId());
													}
												});
												npcs.put(quest.getFirstNPC().getId(), queue);
												queue.startTasks();
											}
										} else if (quest.getRequirements().hasClassTypeRequirement()) {
											if (!RunicCoreHook.isRequiredClass(quest.getRequirements().getClassTypeRequirement(), player)) {
												meetsRequirements = false;
												TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getRequirements().getClassTypeNotMetMsg(), quest.getFirstNPC().getNpcName()));
												queue.setCompletedTask(new Runnable() {
													@Override
													public void run() {
														npcs.remove(quest.getFirstNPC().getId());
													}
												});
												npcs.put(quest.getFirstNPC().getId(), queue);
												queue.startTasks();
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
											npcs.put(quest.getFirstNPC().getId(), queue);
											queue.startTasks();
										}
									}
								}
							}
						} else {
							npcs.get(quest.getFirstNPC().getId()).nextTask();
						}
					}
				}
				if (quest.getFirstNPC().getCitizensNpc().getId() == event.getNPC().getId() &&
						questCooldowns.get(player.getUniqueId().toString()).contains(quest.getQuestID())) {
					int hours = (quest.getCooldown() - (quest.getCooldown() % 3600)) / 3600;
					int minutes = (quest.getCooldown() - (quest.getCooldown() % 60)) / 60 - (hours * 60);
					int seconds = quest.getCooldown() - (hours * 3600) - (minutes * 60);
					StringBuilder time = new StringBuilder();
					time.append(hours == 0 ? "" : hours + " " + (minutes == 0 && hours == 0 ? (hours == 1 ? "hour" : "hours") : (hours == 1 ? "hour, " : "hours, ")));
					time.append(minutes == 0 ? "" : minutes + " " + (seconds == 0 ? (minutes == 1 ? "minute" : "minutes") : (minutes == 1 ? "minute, " : "minutes, ")));
					time.append(seconds == 0 ? "" : seconds + " " + (seconds == 1 ? "second" : "seconds"));
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7You must wait " + time.toString() + " between each completion of this quest!"));
				}
			}
			if (quest.getQuestState().hasStarted()) {
				for (QuestObjective objective : quest.getObjectives()) {
					if (objective.getObjectiveType() == QuestObjectiveType.TALK) {
						QuestObjectiveTalk talkObjective = (QuestObjectiveTalk) objective;
						if (talkObjective.getQuestNpc().getCitizensNpc().getId() == event.getNPC().getId()) {
							if (talkObjective.getQuestNpc().getCitizensNpc().getId() == quest.getFirstNPC().getCitizensNpc().getId()) {
								if (npcs.containsKey(quest.getFirstNPC().getId())) {
									continue;
								}
							}
							if (npcs.containsKey(talkObjective.getQuestNpc().getId())) {
								npcs.get(talkObjective.getQuestNpc().getId()).nextTask();
								break;
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
									if (Plugin.hasQuestItems(objective, player)) {
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
								if (objective.hasCompletedMessage()) {
									for (String message : objective.getCompletedMessage()) {
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', Plugin.parseMessage(message, player.getName())));
									}
								}
								TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, talkObjective.getQuestNpc().getSpeech(), talkObjective.getQuestNpc().getNpcName()));
								queue.setCompletedTask(new Runnable() {
									@Override
									public void run() {
										npcs.remove(talkObjective.getQuestNpc().getId());
									}
								});
								if (objective.getObjectiveNumber() != QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber()) {
									queue.addTasks(new Runnable() {
										@Override
										public void run() {
											String goalMessage = QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage();
											player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 40, 10);
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
										}
									});
								} else {
									queue.addTasks(new Runnable() {
										@Override
										public void run() {
											quest.getQuestState().setCompleted(true);
											questProfile.save();
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() == 1 ? "" : "s")));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() == 1 ? "" : "s")));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience Point" + (quest.getRewards().getExperienceReward() == 1 ? "" : "s")));
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
												if (quest.useLastNpcNameForCompletionSpeech()) {
													TaskQueue secondQueue = new TaskQueue(makeSpeechRunnables(player, quest.getCompletionSpeech(), talkObjective.getQuestNpc().getNpcName()));
													secondQueue.startTasks();
												} else {
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
									});
								}
								npcs.put(talkObjective.getQuestNpc().getId(), queue);
								queue.startTasks();
								break;
							}
						}
					}
				}
			}
		}
		for (Quest quest : questProfile.getQuests()) {
			for (QuestObjective objective : quest.getObjectives()) {
				if (objective.getObjectiveType() == QuestObjectiveType.TALK) {
					QuestObjectiveTalk talkObjective = (QuestObjectiveTalk) objective;
					if (talkObjective.getQuestNpc().getCitizensNpc().getId() == event.getNPC().getId()) {
						if (talkObjective.getQuestNpc().hasIdleSpeech()) {
							for (QuestIdleMessage idleMessage : talkObjective.getQuestNpc().getIdleSpeech()) {
								if (idleMessage.getConditions().hasQuestCompleted()) {
									if (quest.getQuestState().isCompleted() != idleMessage.getConditions().getQuestCompleted()) {
										continue;
									}
								}
								if (idleMessage.getConditions().hasQuestStarted()) {
									if (quest.getQuestState().hasStarted() != idleMessage.getConditions().getQuestStarted()) {
										continue;
									}
								}
								if (idleMessage.getConditions().hasObjectiveStates()) {
									boolean meetsReqs = true;
									objectivesLoop: for (int i = 0; i < quest.getObjectives().size(); i++) {
										if (i < idleMessage.getConditions().getObjectiveStates().size()) {
											if (idleMessage.getConditions().getObjectiveStates().get(i) != null) {
												for (Boolean state : idleMessage.getConditions().getObjectiveStates()) {
													if (quest.getObjectives().get(i).isCompleted() != state) {
														meetsReqs = false;
														break objectivesLoop;
													}
												}
											}
										}
									}
									if (!meetsReqs) {
										continue;
									}
								}
								if (idleMessage.getConditions().hasRequiresQuestItems()) {
									if (Plugin.hasQuestItems(objective, player) != idleMessage.getConditions().requiresQuestItems()) {
										continue;
									}
								}
								TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, idleMessage.getSpeech(), talkObjective.getQuestNpc().getNpcName()));
								queue.setCompletedTask(new Runnable() {
									@Override
									public void run() {
										npcs.remove(talkObjective.getQuestNpc().getId());
									}
								});
								npcs.put(talkObjective.getQuestNpc().getId(), queue);
								queue.startTasks();
								return;
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
			if (quest.getQuestState().hasStarted() == false && quest.getQuestState().isCompleted() == false && quest.getFirstNPC().isDeniable()) {
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
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[" + (messages.indexOf(message) + 1) + "/" + messages.size() + "] &e" + name + ": &6" + Plugin.parseMessage(message, player.getName())));
				}
			});
		}
		return runnables;
	}

}
