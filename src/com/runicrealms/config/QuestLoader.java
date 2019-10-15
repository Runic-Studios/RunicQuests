package com.runicrealms.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.runicrealms.Plugin;
import com.runicrealms.exception.QuestLoadException;
import com.runicrealms.quests.CraftingProfessionType;
import com.runicrealms.quests.ObjectiveTripwire;
import com.runicrealms.quests.PlayerClassType;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestFirstNpc;
import com.runicrealms.quests.QuestIdleMessage;
import com.runicrealms.quests.QuestIdleMessageConditions;
import com.runicrealms.quests.QuestItem;
import com.runicrealms.quests.QuestNpc;
import com.runicrealms.quests.QuestRequirements;
import com.runicrealms.quests.QuestRewards;
import com.runicrealms.quests.objective.QuestObjective;
import com.runicrealms.quests.objective.QuestObjectiveBreak;
import com.runicrealms.quests.objective.QuestObjectiveSlay;
import com.runicrealms.quests.objective.QuestObjectiveTalk;
import com.runicrealms.quests.objective.QuestObjectiveTripwire;

public class QuestLoader {

	private static List<Quest> cachedQuests = null;

	public static List<Quest> getBlankQuestList() {
		if (cachedQuests != null) {
			return cachedQuests;
		}
		List<Quest> quests = new ArrayList<Quest>();
		File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "quests");
		for (File quest : folder.listFiles()) {
			if (!quest.isDirectory()) {
				Quest loadedQuest = null;
				try {
					loadedQuest = QuestLoader.loadQuest(ConfigLoader.getYamlConfigFile(quest.getName(), folder));
				} catch (QuestLoadException exception) {
					exception.addMessage("Error loading quest!");
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

	public static Quest loadQuest(FileConfiguration config) throws QuestLoadException {
		try {
			ArrayList<QuestObjective> objectives = new ArrayList<QuestObjective>();
			int objectivesNumber = checkValueNull(config.getConfigurationSection("objectives"), "objectives").getKeys(false).size();
			for (int i = 1; i <= objectivesNumber; i++) {
				QuestObjective objective;
				try {
					objective = loadObjective(config.getConfigurationSection("objectives." + i), i, objectivesNumber);
				} catch (QuestLoadException exception) {
					exception.addMessage(i + "", "objectives");
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
					(config.contains("completion-message") ? checkValueNull(getStringList(config, "completion-message"), "completion-message") : null),
					(config.contains("use-last-npc-name-for-completion-message") ? checkValueNull(config.getBoolean("use-last-npc-name-for-completion-message"), "use-last-npc-name-for-completion-message") : null),
					(config.getBoolean("repeatable") ? checkValueNull(config.getInt("quest-cooldown"), "quest-cooldown") : null));
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error");
		}
	}

	public static List<QuestIdleMessage> loadIdleMessages(ConfigurationSection configSec, int objectivesNumber) throws QuestLoadException {
		try {
			List<QuestIdleMessage> idleMessages = new ArrayList<QuestIdleMessage>();
			for (String key : configSec.getKeys(false)) {
				List<Boolean> objectives = null;
				if (configSec.contains(key + ".condition.objectives")) {
					objectives = new ArrayList<Boolean>();
					for (int i = 0; i < objectivesNumber; i++) {
						objectives.add(null);
					}
					for (String objectiveNumber : configSec.getConfigurationSection(key + ".condition.objectives").getKeys(false)) {
						objectives.add(Integer.parseInt(objectiveNumber), configSec.getConfigurationSection(key + ".condition.objectives").getBoolean(objectiveNumber));
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

	public static QuestRequirements loadRequirements(ConfigurationSection configSec) throws QuestLoadException {
		try {
			Integer levelReq = checkValueNull(configSec.getInt("level"), "level");
			List<String> levelNotMet = checkValueNull(getStringList(configSec, "level-not-met"), "levle-not-met");
			Integer craftingReq = null;
			List<String> craftingNotMet = null;
			CraftingProfessionType professionType = null;
			if (configSec.contains("blacksmith-level")) {
				craftingNotMet = checkValueNull(getStringList(configSec, "crafting-level-not-met"), "crafting-level-not-met");
				craftingReq = checkValueNull(configSec.getInt("blacksmith-level"), "blacksmith-level");
				professionType = CraftingProfessionType.BLACKSMITH;
			} else if (configSec.contains("jeweling-level")) {
				craftingNotMet = checkValueNull(getStringList(configSec, "crafting-level-not-met"), "crafting-level-not-met");
				craftingReq = checkValueNull(configSec.getInt("enchanter-level"), "enchanter-level");
				professionType = CraftingProfessionType.ENCHANTER;
			} else if (configSec.contains("hunter-level")) {
				craftingNotMet = checkValueNull(getStringList(configSec, "crafting-level-not-met"), "crafting-level-not-met");
				craftingReq = checkValueNull(configSec.getInt("hunter-level"), "hunter-level");
				professionType = CraftingProfessionType.HUNTER;
			} else if (configSec.contains("crafting-level")) {
				craftingNotMet = checkValueNull(getStringList(configSec, "crafting-level-not-met"), "crafting-level-not-met");
				craftingReq = checkValueNull(configSec.getInt("crafting-level"), "crafting-level");
				professionType = CraftingProfessionType.ANY;
			}
			List<Integer> requiredQuests = new ArrayList<Integer>();
			List<String> requiredQuestsNotMet = null;
			if (configSec.contains("required-quests")) {
				requiredQuestsNotMet = checkValueNull(getStringList(configSec, "required-quests-not-met"), "required-quests-not-met");
				if (configSec.isInt("required-quests")) {
					requiredQuests.add(configSec.getInt("required-quests"));
				} else {
					requiredQuests = configSec.getIntegerList("required-quests");
				}
			}
			PlayerClassType classType = configSec.contains("class") ? checkValueNull(PlayerClassType.getFromString(configSec.getString("class")), "class") : null;
			List<String> classTypeNotMet = configSec.contains("class") ? checkValueNull(getStringList(configSec, "class-not-met"), "class") : null;
			return new QuestRequirements(levelReq, craftingReq, professionType, requiredQuests, levelNotMet, craftingNotMet, requiredQuestsNotMet, classType, classTypeNotMet);
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error");
		}
	}

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
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error");
		}
	}

	public static QuestObjective loadObjective(ConfigurationSection configSec, Integer objectiveNumber, int objectivesNumber) throws QuestLoadException {
		try {
			String goalMessage = checkValueNull(configSec.getString("goal-message"), "goal-message");
			if (configSec.getString("requirement.type").equalsIgnoreCase("slay")) {
				if (configSec.contains("requirement.requires")) {
					return new QuestObjectiveSlay(
							checkValueNull(getStringList(configSec, "requirement.mob-names"), "mob-names"), 
							checkValueNull(configSec.getInt("requirement.amount"), "amount"), 
							loadQuestItems(configSec.getConfigurationSection("requirement.requires")),
							goalMessage,
							(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
							objectiveNumber,
							(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
				} else {
					return new QuestObjectiveSlay(
							checkValueNull(getStringList(configSec, "requirement.mob-names"), "mob-names"), 
							checkValueNull(configSec.getInt("requirement.amount"), "amount"), 
							goalMessage,
							(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
							objectiveNumber,
							(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
				}
			} else if (configSec.getString("requirement.type").equalsIgnoreCase("talk")) {
				if (configSec.contains("requirement.requires")) {
					QuestNpc npc;
					try {
						npc = loadNpc(configSec.getConfigurationSection("requirement.npc"), objectivesNumber);
					} catch (QuestLoadException exception) {
						exception.addMessage("npc");
						throw exception;
					}
					return new QuestObjectiveTalk(
							npc,
							loadQuestItems(configSec.getConfigurationSection("requirement.requires")),
							goalMessage,
							(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
							objectiveNumber,
							(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
				} else {
					QuestNpc npc;
					try {
						npc = loadNpc(configSec.getConfigurationSection("requirement.npc"), objectivesNumber);
					} catch (QuestLoadException exception) {
						exception.addMessage("npc");
						throw exception;
					}
					return new QuestObjectiveTalk(
							npc,
							goalMessage,
							(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
							objectiveNumber,
							(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
				}
			} else if (configSec.getString("requirement.type").equalsIgnoreCase("tripwire")) {
				List<ObjectiveTripwire> tripwires = new ArrayList<ObjectiveTripwire>();
				for (String key : configSec.getConfigurationSection("requirement.tripwires").getKeys(false)) {
					tripwires.add(new ObjectiveTripwire(
							checkValueNull(parseLocation(configSec.getString("requirement.tripwires." + key + ".corner-one")), "corner-one"),
							checkValueNull(parseLocation(configSec.getString("requirement.tripwires." + key + ".corner-two")), "corner-two")));
				}
				if (configSec.contains("requirement.requires")) {
					return new QuestObjectiveTripwire(
							tripwires,
							loadQuestItems(configSec.getConfigurationSection("requirement.requires")),
							goalMessage,
							(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
							objectiveNumber,
							(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
				} else {
					return new QuestObjectiveTripwire(
							tripwires,
							goalMessage,
							(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
							objectiveNumber,
							(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
				}
			} else if (configSec.getString("requirement.type").equalsIgnoreCase("break")) {
				if (configSec.contains("requirement.requires")) {
					return new QuestObjectiveBreak(
							checkValueNull(Material.getMaterial(checkValueNull(configSec.getString("requirement.block-type"), "block-type").toUpperCase()), "block-type -> invalid block type"),
							(configSec.contains("requirement.amount") ? configSec.getInt("requirement.amount") : null),
							(configSec.contains("requirement.location") ? parseLocation(configSec.getString("requirement.location")) : null),
							loadQuestItems(configSec.getConfigurationSection("requirement.requires")),
							goalMessage,
							(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
							objectiveNumber,
							(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
				} else {
					return new QuestObjectiveBreak(
							checkValueNull(Material.getMaterial(checkValueNull(configSec.getString("requirement.block-type"), "block-type").toUpperCase()), "block-type -> invalid block type"),
							(configSec.contains("requirement.amount") ? configSec.getInt("requirement.amount") : null),
							(configSec.contains("requirement.location") ? parseLocation(configSec.getString("requirement.location")) : null),
							goalMessage,
							(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
							objectiveNumber,
							(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
				}
			}
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error");
		}
		throw new QuestLoadException("invalid objective type");
	}

	public static QuestFirstNpc loadFirstNpc(ConfigurationSection configSec, int objectivesNumber) throws QuestLoadException {
		try {
			return new QuestFirstNpc(
					checkValueNull(configSec.getInt("npc-id"), "npc-id"),
					checkValueNull(getStringList(configSec, "speech"), "npc-speech"),
					(configSec.contains("idle-messages") ? loadIdleMessages(configSec.getConfigurationSection("idle-messages"), objectivesNumber) : null),
					(configSec.contains("quest-completed-message") ? getStringList(configSec, "quest-completed-message") : null),
					checkValueNull(configSec.getString("npc-name"), "npc-name"),
					(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
					checkValueNull(configSec.getBoolean("deniable"), "deniable"),
					(configSec.getBoolean("deniable") ? checkValueNull(getStringList(configSec, "denied-message"), "denied-message") : null),
					(configSec.getBoolean("deniable") ? checkValueNull(getStringList(configSec, "accepted-message"), "accepted-message") : null));
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error");
		}
	}

	public static QuestNpc loadNpc(ConfigurationSection configSec, int objectivesNumber) throws QuestLoadException {
		try {
			return new QuestNpc(
					checkValueNull(configSec.getInt("npc-id"), "npc-id"),
					checkValueNull(getStringList(configSec, "speech"), "npc-speech"),
					(configSec.contains("idle-messages") ? loadIdleMessages(configSec.getConfigurationSection("idle-messages"), objectivesNumber) : null),
					checkValueNull(configSec.getString("npc-name"), "npc-name"));
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error");
		}
	}

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
		} catch (Exception exception) {
			throw new QuestLoadException("unknown syntax error");
		}
	}

	private static <T> T checkValueNull(T obj, String... message) throws QuestLoadException {
		if (obj == null) {
			throw new QuestLoadException(message);
		}
		return obj;
	}

	private static List<String> getStringList(ConfigurationSection configSec, String path) {
		List<String> list = new ArrayList<String>();
		if (configSec.isString(path)) {
			list.add(configSec.getString(path));
		} else {
			list = configSec.getStringList(path);
		}
		return list;
	}

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
