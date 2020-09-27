package com.runicrealms.runicquests.config;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.exception.QuestLoadException;
import com.runicrealms.runicquests.quests.CraftingProfessionType;
import com.runicrealms.runicquests.quests.PlayerClassType;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestFirstNpc;
import com.runicrealms.runicquests.quests.QuestIdleMessage;
import com.runicrealms.runicquests.quests.QuestIdleMessageConditions;
import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestNpc;
import com.runicrealms.runicquests.quests.QuestRequirements;
import com.runicrealms.runicquests.quests.QuestRewards;
import com.runicrealms.runicquests.quests.location.BoxLocation;
import com.runicrealms.runicquests.quests.location.RadiusLocation;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveBreak;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveLocation;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveSlay;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTrigger;
import com.runicrealms.runicquests.util.NpcPlugin;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class QuestLoader {

	private static List<Quest> cachedQuests = null;

	// Gets a List<Quest> which can only be used as a reference!
	public static List<Quest> getBlankQuestList() {
		if (cachedQuests != null) {
			return cachedQuests;
		}
		return getUnusedQuestList();
	}

	// Gets a List<Quest> from the quests folder, but the quests will contain no user info (like completed, or started)
	public static List<Quest> getUnusedQuestList() {
		if (cachedQuests != null) {
			return obtainNewNpcIds(cachedQuests);
		}
		List<Quest> quests = new ArrayList<Quest>();
		File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "quests");
		for (File quest : folder.listFiles()) {
			if (!quest.isDirectory()) {
				Quest loadedQuest = null;
				try {
					loadedQuest = QuestLoader.loadQuest(ConfigLoader.getYamlConfigFile(quest.getName(), folder));
				} catch (QuestLoadException exception) {
					exception.addMessage("Error loading quest: " + quest.getName());
					exception.displayToConsole();
					exception.displayToOnlinePlayers();
				} finally {
					quests.add(loadedQuest);
				}
			}
		}
		cachedQuests = quests;
		return quests;
	}

	// Method for creating a new List<Quest> with new NPC IDs and reset FirstNpcState
	public static List<Quest> obtainNewNpcIds(List<Quest> quests) {
		List<Quest> newQuests = new ArrayList<Quest>();
		for (Quest quest : quests) {
			if (quest != null) {
				Quest newQuest = quest.clone();
				newQuests.add(newQuest);
			} else {
				newQuests.add(null);
			}
		}
		return newQuests;
	}

	// Uses all methods from this class in order to load a quest from file. Will not have any player info.
	public static Quest loadQuest(FileConfiguration config) throws QuestLoadException {
		try {
			ArrayList<QuestObjective> objectives = new ArrayList<QuestObjective>();
			int objectivesNumber = checkValueNull(config.getConfigurationSection("objectives"), "objectives").getKeys(false).size();
			for (int i = 1; i <= objectivesNumber; i++) {
				QuestObjective objective;
				try {
					objective = loadObjective(config.getConfigurationSection("objectives." + i), i, objectivesNumber);
				} catch (QuestLoadException exception) {
					exception.addMessage(i + "", "objective: " + i);
					throw exception;
				}
				objectives.add(objective);
			}
			QuestFirstNpc firstNPC;
			try {
				firstNPC = loadFirstNpc(config.getConfigurationSection("first-npc"), objectivesNumber);
			} catch (QuestLoadException exception) {
				exception.addMessage("first-npc");
				throw exception;
			}
			QuestRewards rewards;
			try {
				rewards = loadRewards(config.getConfigurationSection("rewards"));
			} catch (QuestLoadException exception) {
				exception.addMessage("rewards");
				throw exception;
			}
			QuestRequirements requirements;
			try {
				requirements = loadRequirements(config.getConfigurationSection("requirements"));
			} catch (QuestLoadException exception) {
				exception.addMessage("requirements");
				throw exception;
			}
			return new Quest(
					checkValueNull(config.getString("name"), "name"),
					firstNPC,
					objectives,
					rewards,
					checkValueNull(config.getInt("unique-id"), "unique-id"),
					requirements,
					checkValueNull(config.getBoolean("side-quest"), "side-quest"),
					checkValueNull(config.getBoolean("repeatable"), "repeatable"),
					(config.getBoolean("repeatable") ? checkValueNull(config.getInt("quest-cooldown"), "quest-cooldown") : null));
		} catch (QuestLoadException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
		}
	}

	// Loads idle messages.
	public static List<QuestIdleMessage> loadIdleMessages(ConfigurationSection configSec, int objectivesNumber) throws QuestLoadException {
		try {
			List<QuestIdleMessage> idleMessages = new ArrayList<QuestIdleMessage>();
			for (String key : configSec.getKeys(false)) {
				List<Boolean> objectives = null;
				if (configSec.contains(key + ".condition.objectives")) {
					objectives = new ArrayList<Boolean>();
					for (int i = 0; i <= objectivesNumber; i++) {
						objectives.add(null);
					}
					for (String objectiveNumber : configSec.getConfigurationSection(key + ".condition.objectives").getKeys(false)) {
						objectives.set(Integer.parseInt(objectiveNumber), configSec.getConfigurationSection(key + ".condition.objectives").getBoolean(objectiveNumber));
					}
				}
				QuestIdleMessageConditions conditions = new QuestIdleMessageConditions(
						(configSec.contains(key + ".condition.quest.started") ? configSec.getBoolean(key + ".condition.quest.started") : null),
						(configSec.contains(key + ".condition.quest.completed") ? configSec.getBoolean(key + ".condition.quest.completed") : null),
						objectives,
						(configSec.contains(key + ".condition.quest-items") ? configSec.getBoolean(key + ".condition.quest-items") : null));
				idleMessages.add(new QuestIdleMessage(conditions, getStringList(configSec, key + ".speech")));
			}
			return idleMessages;
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error", "idle-messages");
		}
	}

	// Loads quest requirements.
	public static QuestRequirements loadRequirements(ConfigurationSection configSec) throws QuestLoadException {
		try {
			Integer levelReq = checkValueNull(configSec.getInt("level"), "level");
			List<String> levelNotMet = configSec.contains("level-not-met") ? getStringList(configSec, "level-not-met") : null;
			Integer craftingReq = null;
			List<String> craftingNotMet = null;
			List<CraftingProfessionType> professionType = new ArrayList<CraftingProfessionType>();
			if (configSec.contains("blacksmith-level")) {
				craftingNotMet = configSec.contains("crafting-level-not-met") ? getStringList(configSec, "crafting-level-not-met") : null;
				craftingReq = checkValueNull(configSec.getInt("blacksmith-level"), "blacksmith-level");
				professionType.add(CraftingProfessionType.BLACKSMITH);
			}
			if (configSec.contains("jeweling-level")) {
				craftingNotMet = configSec.contains("crafting-level-not-met") ? getStringList(configSec, "crafting-level-not-met") : null;
				craftingReq = checkValueNull(configSec.getInt("enchanter-level"), "enchanter-level");
				professionType.add(CraftingProfessionType.JEWELER);
			}
			if (configSec.contains("hunter-level")) {
				craftingNotMet = configSec.contains("crafting-level-not-met") ? getStringList(configSec, "crafting-level-not-met") : null;
				craftingReq = checkValueNull(configSec.getInt("hunter-level"), "hunter-level");
				professionType.add(CraftingProfessionType.HUNTER);
			}
			if (configSec.contains("alchemist-level")) {
				craftingNotMet = configSec.contains("crafting-level-not-met") ? getStringList(configSec, "crafting-level-not-met") : null;
				craftingReq = checkValueNull(configSec.getInt("alchemist-level"), "alchemist-level");
				professionType.add(CraftingProfessionType.ALCHEMIST);
			}
			if (configSec.contains("enchanter-level")) {
				craftingNotMet = configSec.contains("crafting-level-not-met") ? getStringList(configSec, "crafting-level-not-met") : null;
				craftingReq = checkValueNull(configSec.getInt("enchanter-level"), "enchanter-level");
				professionType.add(CraftingProfessionType.ENCHANTER);
			}
			if (configSec.contains("crafting-level")) {
				craftingNotMet = configSec.contains("crafting-level-not-met") ? getStringList(configSec, "crafting-level-not-met") : null;
				craftingReq = checkValueNull(configSec.getInt("crafting-level"), "crafting-level");
				professionType.add(CraftingProfessionType.ANY);
			}
			List<Integer> requiredQuests = new ArrayList<Integer>();
			List<String> requiredQuestsNotMet = null;
			if (configSec.contains("required-quests")) {
				requiredQuestsNotMet = configSec.contains("required-quests-not-met") ? getStringList(configSec, "required-quests-not-met") : null;
				if (configSec.isInt("required-quests")) {
					requiredQuests.add(configSec.getInt("required-quests"));
				} else {
					requiredQuests = configSec.getIntegerList("required-quests");
				}
			}
			PlayerClassType classType = configSec.contains("class") ? checkValueNull(PlayerClassType.getFromString(configSec.getString("class")), "class") : null;
			List<String> classTypeNotMet = configSec.contains("class-not-met") ? getStringList(configSec, "class-not-met") : null;
			return new QuestRequirements(levelReq, craftingReq, professionType, requiredQuests, levelNotMet, craftingNotMet, requiredQuestsNotMet, classType, classTypeNotMet);
		} catch (QuestLoadException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
		}
	}

	// Loads quest rewards
	public static QuestRewards loadRewards(ConfigurationSection configSec) throws QuestLoadException {
		try {
			List<String> execute = null;
			if (configSec.contains("execute")) {
				execute = new ArrayList<String>();
				if (configSec.isString("execute")) {
					execute.add(configSec.getString("execute"));
				} else {
					execute = configSec.getStringList("execute");
				}
			}
			return new QuestRewards(
					checkValueNull(configSec.getInt("exp"), "exp"),
					checkValueNull(configSec.getInt("quest-points"), "quest-points"),
					checkValueNull(configSec.getInt("money"), "money"),
					execute);
		} catch (QuestLoadException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
		}
	}

	// Loads a quest objective
	public static QuestObjective loadObjective(ConfigurationSection configSec, Integer objectiveNumber, int objectivesNumber) throws QuestLoadException {
		try {
			String goalMessage = checkValueNull(configSec.getString("goal-message"), "goal-message");
			String goalLocation = configSec.contains("goal-location") ? configSec.getString("goal-location") : "";
			if (configSec.getString("requirement.type").equalsIgnoreCase("slay")) {
				return new QuestObjectiveSlay(
						checkValueNull(getStringList(configSec, "requirement.mob-names"), "mob-names"),
						checkValueNull(configSec.getInt("requirement.amount"), "amount"),
						configSec.contains("requirement.requires") ? loadQuestItems(configSec.getConfigurationSection("requirement.requires")) : null,
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null),
						goalLocation);
			} else if (configSec.getString("requirement.type").equalsIgnoreCase("talk")) {
				QuestNpc npc;
				try {
					npc = loadNpc(configSec.getConfigurationSection("requirement.npc"), objectivesNumber);
				} catch (QuestLoadException exception) {
					exception.addMessage("npc");
					throw exception;
				}
				return new QuestObjectiveTalk(
						npc,
						configSec.contains("requirement.requires") ? loadQuestItems(configSec.getConfigurationSection("requirement.requires")) : null,
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null),
						goalLocation);
			} else if (configSec.getString("requirement.type").equalsIgnoreCase("location")) {
				if (checkValueNull(configSec.getString("requirement.location-type")).equalsIgnoreCase("radius")) {
					return new QuestObjectiveLocation(
							new RadiusLocation(checkValueNull(parseLocation(configSec.getString("requirement.location"))), checkValueNull(configSec.getInt("requirement.radius"))),
							configSec.contains("requirement.requires") ? loadQuestItems(configSec.getConfigurationSection("requirement.requires")) : null,
							goalMessage,
							(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
							objectiveNumber,
							(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null),
							goalLocation);
				} else {
					return new QuestObjectiveLocation(
							new BoxLocation(checkValueNull(parseLocation(configSec.getString("requirement.corner-one"))), checkValueNull(parseLocation(configSec.getString("requirement.corner-two")))),
							configSec.contains("requirement.requires") ? loadQuestItems(configSec.getConfigurationSection("requirement.requires")) : null,
							goalMessage,
							(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
							objectiveNumber,
							(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null),
							goalLocation);

				}
			} else if (configSec.getString("requirement.type").equalsIgnoreCase("break")) {
				return new QuestObjectiveBreak(
						checkValueNull(Material.getMaterial(checkValueNull(configSec.getString("requirement.block-type"), "block-type").toUpperCase()), "block-type -> invalid block type"),
						(configSec.contains("requirement.amount") ? configSec.getInt("requirement.amount") : null),
						(configSec.contains("requirement.location") ? parseLocation(configSec.getString("requirement.location")) : null),
						configSec.contains("requirement.requires") ? loadQuestItems(configSec.getConfigurationSection("requirement.requires")) : null,
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null),
						goalLocation);
			} else if (configSec.getString("requirement.type").equalsIgnoreCase("trigger")) {
				return new QuestObjectiveTrigger(
						checkValueNull(configSec.getString("requirement.trigger-id")),
						checkValueNull(getStringList(configSec, "requirement.speech")),
						configSec.contains("requirement.requires") ? loadQuestItems(configSec.getConfigurationSection("requirement.requires")) : null,
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null),
						goalLocation);
			}
		} catch (QuestLoadException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
		}
		throw new QuestLoadException("invalid objective type");
	}

	// Loads a quest first NPC
	public static QuestFirstNpc loadFirstNpc(ConfigurationSection configSec, int objectivesNumber) throws QuestLoadException {
		try {
			NpcPlugin plugin = (configSec.contains("plugin") ? NpcPlugin.getFromString(configSec.getString("plugin"), NpcPlugin.CITIZENS) : NpcPlugin.CITIZENS);
			return new QuestFirstNpc(
					checkValueNull(configSec.getInt("npc-id"), "npc-id"),
					(plugin == NpcPlugin.CITIZENS ? CitizensAPI.getNPCRegistry().getById(configSec.getInt("npc-id")).getStoredLocation() : com.runicrealms.runicnpcs.Plugin.getNpcs().get(configSec.getInt("npc-id")).getLocation()),
					checkValueNull(getStringList(configSec, "speech"), "npc-speech"),
					(configSec.contains("idle-messages") ? loadIdleMessages(configSec.getConfigurationSection("idle-messages"), objectivesNumber) : null),
					(configSec.contains("quest-completed-message") ? getStringList(configSec, "quest-completed-message") : null),
					checkValueNull(configSec.getString("npc-name"), "npc-name"),
					(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
					plugin,
					configSec.contains("goal-message") ? configSec.getString("goal-message") : null,
					configSec.contains("goal-location") ? configSec.getString("goal-location") : null);
		} catch (QuestLoadException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
		}
	}

	// Loads a quest objective NPC
	public static QuestNpc loadNpc(ConfigurationSection configSec, int objectivesNumber) throws QuestLoadException {
		try {
			return new QuestNpc(
					checkValueNull(configSec.getInt("npc-id"), "npc-id"),
					checkValueNull(getStringList(configSec, "speech"), "npc-speech"),
					(configSec.contains("idle-messages") ? loadIdleMessages(configSec.getConfigurationSection("idle-messages"), objectivesNumber) : null),
					checkValueNull(configSec.getString("npc-name"), "npc-name"),
					(configSec.contains("plugin") ? NpcPlugin.getFromString(configSec.getString("plugin"), NpcPlugin.CITIZENS) : NpcPlugin.CITIZENS),
					(configSec.contains("denied-message") ? getStringList(configSec, "denied-message") : null));
		} catch (QuestLoadException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
		}
	}

	// Loads required quest items
	public static List<QuestItem> loadQuestItems(ConfigurationSection configSec) throws QuestLoadException {
		try {
			List<QuestItem> questItems = new ArrayList<QuestItem>();
			for (String key : configSec.getKeys(false)) {
				questItems.add(new QuestItem(
						checkValueNull(configSec.getString(key + ".item-name"), "item-name", key, "requires"),
						checkValueNull(configSec.getString(key + ".item-type"), "item-type", key, "requires"),
						checkValueNull(configSec.getInt(key + ".item-count"), "item-count", key, "requires")));
			}
			return questItems;
		} catch (QuestLoadException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
		}
	}

	// Throws an exception if the given value is null
	private static <T> T checkValueNull(T obj, String... message) throws QuestLoadException {
		if (obj == null) {
			throw new QuestLoadException(message);
		}
		return obj;
	}

	// Gets a string/string list from config file
	private static List<String> getStringList(ConfigurationSection configSec, String path) {
		List<String> list = new ArrayList<String>();
		if (configSec.isString(path)) {
			list.add(configSec.getString(path));
		} else {
			list = configSec.getStringList(path);
		}
		return list;
	}

	// Parses a location from a string
	private static Location parseLocation(String str) {
		World world;
		double x, y, z;
		try {
			world = Bukkit.getWorld(str.split(",")[0].replaceAll(",", ""));
			x = Double.parseDouble(str.split(",")[1].replaceAll(",", ""));
			y = Double.parseDouble(str.split(",")[2].replaceAll(",", ""));
			z = Double.parseDouble(str.split(",")[3].replaceAll(",", ""));
		} catch (Exception exception) {
			return null;
		}
		return new Location(world, x, y, z);
	}

}
