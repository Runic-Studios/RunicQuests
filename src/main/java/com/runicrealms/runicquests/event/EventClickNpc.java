package com.runicrealms.runicquests.event;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.runicrealms.runiccharacters.api.events.CharacterNPCLeftClickEvent;
import com.runicrealms.runiccharacters.api.events.CharacterNPCRightClickEvent;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.api.QuestAcceptEvent;
import com.runicrealms.runicquests.api.QuestCompleteEvent;
import com.runicrealms.runicquests.api.QuestDenyEvent;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.FirstNpcState;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestIdleMessage;
import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveBreak;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveSlay;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;
import com.runicrealms.runicquests.task.TaskQueue;
import com.runicrealms.runicquests.util.RunicCoreHook;

public class EventClickNpc implements Listener {

	@EventHandler
	public void onNpcRightClick(CharacterNPCRightClickEvent event) {
		Player player = event.getClicker();
		QuestProfile questProfile = Plugin.getQuestProfile(player.getUniqueId().toString());
		HashMap<Long, TaskQueue> npcs = Plugin.getNpcTaskQueues();
		Map<UUID, Map<Integer, List<Integer>>> questCooldowns = Plugin.getQuestCooldowns();
		for (Quest quest : questProfile.getQuests()) { // Loop through quests to find a match for the NPC
			if (quest.getQuestState().isCompleted() && quest.isRepeatable() == false) { // Check for if the quest is completed
				if (quest.getFirstNPC().getCitizensNpc().getId() == event.getNPC().getId()) { // Check for first NPC quest completed speech 
					if (quest.getFirstNPC().hasQuestCompletedSpeech()) { // Create a task queue for the speech
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
					(quest.isRepeatable() && quest.getQuestState().hasStarted() && quest.getQuestState().isCompleted())) { // Check that the quest is not completed
				if (quest.getFirstNPC().getCitizensNpc().getId() == event.getNPC().getId() &&
						questCooldowns.get(player.getUniqueId()).get(event.getCharacter().getSlot()).contains(quest.getQuestID()) == false) { // Check for an NPC id match between the first NPC and the clicked NPC
					if (QuestObjective.getObjective(quest.getObjectives(), 1).isCompleted() == false || quest.isRepeatable()) { // Check that the first objective has not been completed
						if (!npcs.containsKey(quest.getFirstNPC().getId())) { // Check that the player is not currently talking with the NPC
							if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED || (quest.isRepeatable() && Plugin.allObjectivesComplete(quest))) { // Check that the player has not yet accepted the quest
								if (quest.isRepeatable()) { // Check if the quest is repeatable
									for (QuestObjective qobjective : quest.getObjectives()) { // Reset all the objective stats (mobs killed and blocks broken)
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
								if (!quest.getFirstNPC().isDeniable()) { // Check if you can left click the NPC to deny
									quest.getQuestState().setStarted(true);
									questProfile.save();
									if (quest.getFirstNPC().hasExecute()) { // Execute the first NPC commands
										quest.getFirstNPC().executeCommand(player.getName());
									}
									TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getFirstNPC().getSpeech(), quest.getFirstNPC().getNpcName())); // Create a task queue with the first NPC speech
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
								} else { // If the quest is not deniable then...
									if (quest.getFirstNPC().getState() == FirstNpcState.PENDING || quest.getFirstNPC().getState() == FirstNpcState.DENIED) { // Check that the NPC is waiting for a response
										quest.getQuestState().setStarted(true);
										questProfile.save();
										if (quest.getFirstNPC().hasExecute()) { // Execute the first NPC commands
											quest.getFirstNPC().executeCommand(player.getName());
										}
										quest.getFirstNPC().setState(FirstNpcState.ACCEPTED);
										questProfile.save();
										TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getFirstNPC().getAcceptedMessage(), quest.getFirstNPC().getNpcName())); // Create a task queue with the accepted-message
										queue.addTasks(new Runnable() {
											@Override
											public void run() {
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
												player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + QuestObjective.getObjective(quest.getObjectives(), 1).getGoalMessage()));
											}
										});
										queue.startTasks();
										Bukkit.getServer().getPluginManager().callEvent(new QuestAcceptEvent(quest, questProfile)); // Fire the quest accept event
									} else if (quest.getFirstNPC().getState() == FirstNpcState.NEUTRAL) { // If the NPC is not waiting for a response
										boolean meetsRequirements = true;
										if (!RunicCoreHook.isRequiredLevel(player, quest.getRequirements().getLevelRequirement())) { // Check that the player is the required level
											meetsRequirements = false;
											TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getRequirements().getLevelNotMetMsg(), quest.getFirstNPC().getNpcName())); // Create a task queue with the level not met message
											queue.setCompletedTask(new Runnable() {
												@Override
												public void run() {
													npcs.remove(quest.getFirstNPC().getId());
												}
											});
											npcs.put(quest.getFirstNPC().getId(), queue);
											queue.startTasks();
										} else if (quest.getRequirements().hasCraftingRequirement()) { // Check if the quest has a crafting requirement
											if (!RunicCoreHook.isRequiredCraftingLevel(player, quest.getRequirements().getCraftingProfessionType(), quest.getRequirements().getCraftingRequirement())) { // Check that the player is the required crafting level
												meetsRequirements = false;
												TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getRequirements().getCraftingLevelNotMetMsg(), quest.getFirstNPC().getNpcName())); // Create a task queue with the crafting not met message
												queue.setCompletedTask(new Runnable() {
													@Override
													public void run() {
														npcs.remove(quest.getFirstNPC().getId());
													}
												});
												npcs.put(quest.getFirstNPC().getId(), queue);
												queue.startTasks();

											}
										} else if (quest.getRequirements().hasCompletedQuestRequirement()) { // Check if the quest has a completed quests requirement
											if (!RunicCoreHook.hasCompletedRequiredQuests(player, quest.getRequirements().getCompletedQuestsRequirement())) { // Check that player has completed the required quests
												meetsRequirements = false;
												TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getRequirements().getCompletedQuestsNotMetMsg(), quest.getFirstNPC().getNpcName())); // Create a task queue with the quests completed not met message
												queue.setCompletedTask(new Runnable() {
													@Override
													public void run() {
														npcs.remove(quest.getFirstNPC().getId());
													}
												});
												npcs.put(quest.getFirstNPC().getId(), queue);
												queue.startTasks();
											}
										} else if (quest.getRequirements().hasClassTypeRequirement()) { // Check if the quest has a class requirement
											if (!RunicCoreHook.isRequiredClass(quest.getRequirements().getClassTypeRequirement(), player)) { // Check that the player is the required class
												meetsRequirements = false;
												TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getRequirements().getClassTypeNotMetMsg(), quest.getFirstNPC().getNpcName())); // Create a task queue with the class type not met message
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
										if (meetsRequirements) { // Check that the player meets the requirements
											List<Runnable> runnables = makeSpeechRunnables(player, quest.getFirstNPC().getSpeech(), quest.getFirstNPC().getNpcName()); // Creates a task queue with the first NPC speech
											runnables.add(new Runnable() {
												@Override
												public void run() {
													player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Left click to deny quest, right click to accept"));
												}
											});
											TaskQueue queue = new TaskQueue(runnables);
											queue.setCompletedTask(new Runnable() { // On completion, set the first NPC status to pending
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
						} else { // If the player IS talking to this NPC currently, then...
							npcs.get(quest.getFirstNPC().getId()).nextTask(); // Move to next speech line
						}
					}
				}
				if (quest.getFirstNPC().getCitizensNpc().getId() == event.getNPC().getId() &&
						questCooldowns.get(player.getUniqueId()).get(event.getCharacter().getSlot()).contains(quest.getQuestID())) { // If the player is waiting on a quest cooldown (repeatable quests)
					int hours = (quest.getCooldown() - (quest.getCooldown() % 3600)) / 3600; // Some very odd code to create a cooldown message
					int minutes = (quest.getCooldown() - (quest.getCooldown() % 60)) / 60 - (hours * 60);
					int seconds = quest.getCooldown() - (hours * 3600) - (minutes * 60);
					StringBuilder time = new StringBuilder();
					time.append(hours == 0 ? "" : hours + " " + (minutes == 0 && hours == 0 ? (hours == 1 ? "hour" : "hours") : (hours == 1 ? "hour, " : "hours, ")));
					time.append(minutes == 0 ? "" : minutes + " " + (seconds == 0 ? (minutes == 1 ? "minute" : "minutes") : (minutes == 1 ? "minute, " : "minutes, ")));
					time.append(seconds == 0 ? "" : seconds + " " + (seconds == 1 ? "second" : "seconds"));
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7You must wait " + time.toString() + " between each completion of this quest!"));
				}
			}
			if (quest.getQuestState().hasStarted()) { // If the quest has started...
				for (QuestObjective objective : quest.getObjectives()) { // Loop through the objectives
					if (objective.getObjectiveType() == QuestObjectiveType.TALK) { // Check the objective type
						QuestObjectiveTalk talkObjective = (QuestObjectiveTalk) objective;
						if (talkObjective.getQuestNpc().getCitizensNpc().getId() == event.getNPC().getId()) { // Check that the NPC id matches the one that has been clicked
							if (talkObjective.getQuestNpc().getCitizensNpc().getId() == quest.getFirstNPC().getCitizensNpc().getId()) { // Check if the NPC being talked to is the first NPC (same NPC used twice)
								if (npcs.containsKey(quest.getFirstNPC().getId())) { // If you are talking to the first NPC, continue to next objective
									continue;
								}
							}
							if (npcs.containsKey(talkObjective.getQuestNpc().getId())) { // If you are talking to the NPC...
								npcs.get(talkObjective.getQuestNpc().getId()).nextTask(); // Move to next speech line
								break;
							}
							if (objective.isCompleted() == false) { // Check that the objective isn't completed
								if (objective.getObjectiveNumber() != 1) { // Check that the previous objective has been completed
									if (QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 1).isCompleted() == false) {
										continue;
									}
								}
								if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) { // Check that you have accepted the quest
									continue;
								}
								if (objective.requiresQuestItem()) { // Check for a required quest item, remove it from inventory
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
								if (objective.hasCompletedMessage()) { // Display completed message if there is one
									for (String message : objective.getCompletedMessage()) {
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', Plugin.parseMessage(message, player.getName())));
									}
								}
								TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, talkObjective.getQuestNpc().getSpeech(), talkObjective.getQuestNpc().getNpcName())); // Put the NPC speech in a task queue
								queue.setCompletedTask(new Runnable() {
									@Override
									public void run() {
										npcs.remove(talkObjective.getQuestNpc().getId());
									}
								});
								if (objective.getObjectiveNumber() != QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber()) { // Check that this is not the last objective
									queue.addTasks(new Runnable() { // Add the new objective message to the task queue
										@Override
										public void run() {
											String goalMessage = QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage();
											player.sendTitle(ChatColor.GOLD + "New Objective", ChatColor.YELLOW + goalMessage, 10, 40, 10);
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + goalMessage));
										}
									});
								} else { // If this is the last objective then...
									queue.addTasks(new Runnable() { // Add the quest rewards to the task queue
										@Override
										public void run() {
											quest.getQuestState().setCompleted(true);
											questProfile.save();
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() == 1 ? "" : "s")));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() == 1 ? "" : "s")));
											player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience Point" + (quest.getRewards().getExperienceReward() == 1 ? "" : "s")));
											if (quest.getRewards().hasExecute()) { // Execute the quest rewards commands
												quest.getRewards().executeCommand(player.getName());
											}
											RunicCoreHook.giveRewards(player, quest.getRewards()); // Give the rewards
											if (quest.isRepeatable() == true) { // The the quest is repeatable, then handle the cooldowns
												questCooldowns.get(player.getUniqueId()).get(event.getCharacter().getSlot()).get(quest.getQuestID());
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
									});
								}
								npcs.put(talkObjective.getQuestNpc().getId(), queue); // Add the queue to the NPCs that are being talked to
								queue.startTasks();
								break;
							}
						}
					}
				}
			}
		}
		for (Quest quest : questProfile.getQuests()) { // Loop through the quests, check for idle messages
			for (QuestObjective objective : quest.getObjectives()) { // Loop through objectives
				if (objective.getObjectiveType() == QuestObjectiveType.TALK) { // Check for objective of type talk
					QuestObjectiveTalk talkObjective = (QuestObjectiveTalk) objective;
					if (talkObjective.getQuestNpc().getCitizensNpc().getId() == event.getNPC().getId()) { // Check that the NPC id matches the one clicked on
						if (talkObjective.getQuestNpc().hasIdleSpeech()) { // Check for idle speech
							idleMessageLoop: for (QuestIdleMessage idleMessage : talkObjective.getQuestNpc().getIdleSpeech()) { // Loop through idle messages
								if (idleMessage.getConditions().hasQuestCompleted()) { // Check for quest completed condition
									if (quest.getQuestState().isCompleted() != idleMessage.getConditions().getQuestCompleted()) {
										continue idleMessageLoop;
									}
								}
								if (idleMessage.getConditions().hasQuestStarted()) { // Check for quest started condition
									if (quest.getQuestState().hasStarted() != idleMessage.getConditions().getQuestStarted()) {
										continue idleMessageLoop;
									}
								}
								if (idleMessage.getConditions().hasObjectiveStates()) { // Check for objectives completed condition
									for (int i = 0; i < idleMessage.getConditions().getObjectiveStates().size(); i++) {
										if (i < quest.getObjectives().size()) {
											if (idleMessage.getConditions().getObjectiveStates().get(i) != null) {
												if (quest.getObjectives().get(i).isCompleted() != idleMessage.getConditions().getObjectiveStates().get(i)) {
													continue idleMessageLoop;
												}
											}
										}
									}
								}
								if (idleMessage.getConditions().hasRequiresQuestItems()) { // Check for requires quest items condition
									if (Plugin.hasQuestItems(objective, player) != idleMessage.getConditions().requiresQuestItems()) {
										continue idleMessageLoop;
									}
								}
								TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, idleMessage.getSpeech(), talkObjective.getQuestNpc().getNpcName())); // Creates a task queue with the idle message
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
	public void onNpcLeftClick(CharacterNPCLeftClickEvent event) {
		Player player = event.getClicker();
		QuestProfile questProfile = Plugin.getQuestProfile(event.getClicker().getUniqueId().toString()); // Get the player's questing profile
		for (Quest quest : questProfile.getQuests()) { // Loop through the quests
			if (quest.getQuestState().hasStarted() == false && quest.getQuestState().isCompleted() == false && quest.getFirstNPC().isDeniable()) { // Check that the quest has not been started, is not completed, and is deniable
				if (quest.getFirstNPC().getCitizensNpc().getId() == event.getNPC().getId()) { // Check for a match between the NPC id and the NPC clicked on
					if (QuestObjective.getObjective(quest.getObjectives(), 1).isCompleted() == false) { // Checks that the first objective has not been completed (you have not yet started the quest)
						quest.getFirstNPC().setState(FirstNpcState.DENIED); // Denies the quest
						questProfile.save();
						TaskQueue queue = new TaskQueue(makeSpeechRunnables(player, quest.getFirstNPC().getDeniedMessage(), quest.getFirstNPC().getNpcName())); // Displays the denied-message to the player
						queue.startTasks();
						Bukkit.getServer().getPluginManager().callEvent(new QuestDenyEvent(quest, questProfile)); // Fires the quest deny event
					}
				}
			}
		}
	}

	private static List<Runnable> makeSpeechRunnables(Player player, List<String> messages, String name) { // Formats the text to look like an NPC message. Creates List<Runnable> for a task queue.
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