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
				quests.add(QuestLoader.loadQuest(ConfigLoader.getYamlConfigFile(quest.getName(), folder)));
			}
		}
		cachedQuests = quests;
		return quests;
	}

	public static Quest loadQuest(FileConfiguration config) {
		ArrayList<QuestObjective> objectives = new ArrayList<QuestObjective>();
		int objectivesNumber = config.getConfigurationSection("objectives").getKeys(false).size();
		for (int i = 1; i <= objectivesNumber; i++) {
			objectives.add(loadObjective(config.getConfigurationSection("objectives." + i), i, objectivesNumber));
		}
		return new Quest(
				config.getString("name"),
				loadFirstNpc(config.getConfigurationSection("first-npc"), objectivesNumber),
				objectives,
				loadRewards(config.getConfigurationSection("rewards")),
				config.getInt("unique-id"),
				loadRequirements(config.getConfigurationSection("requirements")),
				config.getBoolean("side-quest"),
				config.getBoolean("repeatable"),
				(config.contains("completion-message") ? getStringList(config, "completion-message") : null),
				(config.contains("use-last-npc-name-for-completion-message") ? config.getBoolean("use-last-npc-name-for-completion-message") : null),
				(config.getBoolean("repeatable") ? config.getInt("quest-cooldown") : null));
	}

	public static List<QuestIdleMessage> loadIdleMessages(ConfigurationSection configSec, int objectivesNumber) {
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
	}

	public static QuestRequirements loadRequirements(ConfigurationSection configSec) {
		Integer levelReq = configSec.getInt("level");
		List<String> levelNotMet = getStringList(configSec, "level-not-met");
		Integer craftingReq = null;
		List<String> craftingNotMet = null;
		CraftingProfessionType professionType = null;
		if (configSec.contains("blacksmith-level")) {
			craftingNotMet = getStringList(configSec, "crafting-level-not-met");
			craftingReq = configSec.getInt("blacksmith-level");
			professionType = CraftingProfessionType.BLACKSMITH;
		} else if (configSec.contains("jeweling-level")) {
			craftingNotMet = getStringList(configSec, "crafting-level-not-met");
			craftingReq = configSec.getInt("enchanter-level");
			professionType = CraftingProfessionType.ENCHANTER;
		} else if (configSec.contains("hunter-level")) {
			craftingNotMet = getStringList(configSec, "crafting-level-not-met");
			craftingReq = configSec.getInt("hunter-level");
			professionType = CraftingProfessionType.HUNTER;
		} else if (configSec.contains("crafting-level")) {
			craftingNotMet = getStringList(configSec, "crafting-level-not-met");
			craftingReq = configSec.getInt("crafting-level");
			professionType = CraftingProfessionType.ANY;
		}
		List<Integer> requiredQuests = new ArrayList<Integer>();
		List<String> requiredQuestsNotMet = null;
		if (configSec.contains("required-quests")) {
			requiredQuestsNotMet = getStringList(configSec, "required-quests-not-met");
			if (configSec.isInt("required-quests")) {
				requiredQuests.add(configSec.getInt("required-quests"));
			} else {
				requiredQuests = configSec.getIntegerList("required-quests");
			}
		}
		PlayerClassType classType = configSec.contains("class") ? PlayerClassType.getFromString(configSec.getString("class")) : null;
		List<String> classTypeNotMet = configSec.contains("class") ? getStringList(configSec, "class-not-met") : null;
		return new QuestRequirements(levelReq, craftingReq, professionType, requiredQuests, levelNotMet, craftingNotMet, requiredQuestsNotMet, classType, classTypeNotMet);
	}

	public static QuestRewards loadRewards(ConfigurationSection configSec) {
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
				configSec.getInt("exp"),
				configSec.getInt("quest-points"),
				configSec.getInt("money"),
				execute);
	}

	public static QuestObjective loadObjective(ConfigurationSection configSec, Integer objectiveNumber, int objectivesNumber) {
		String goalMessage = configSec.getString("goal-message");
		if (configSec.getString("requirement.type").equalsIgnoreCase("slay")) {
			if (configSec.contains("requirement.requires")) {
				return new QuestObjectiveSlay(
						getStringList(configSec, "requirement.mob-names"), 
						configSec.getInt("requirement.amount"), 
						getQuestItems(configSec.getConfigurationSection("requirement.requires")),
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			} else {
				return new QuestObjectiveSlay(
						getStringList(configSec, "requirement.mob-names"), 
						configSec.getInt("requirement.amount"), 
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			}
		} else if (configSec.getString("requirement.type").equalsIgnoreCase("talk")) {
			if (configSec.contains("requirement.requires")) {
				return new QuestObjectiveTalk(
						loadNpc(configSec.getConfigurationSection("requirement.npc"), objectivesNumber),
						getQuestItems(configSec.getConfigurationSection("requirement.requires")),
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			} else {
				return new QuestObjectiveTalk(
						loadNpc(configSec.getConfigurationSection("requirement.npc"), objectivesNumber),
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			}
		} else if (configSec.getString("requirement.type").equalsIgnoreCase("tripwire")) {
			List<ObjectiveTripwire> tripwires = new ArrayList<ObjectiveTripwire>();
			for (String key : configSec.getConfigurationSection("requirement.tripwires").getKeys(false)) {
				tripwires.add(new ObjectiveTripwire(
						parseLocation(configSec.getString("requirement.tripwires." + key + ".corner-one")),
						parseLocation(configSec.getString("requirement.tripwires." + key + ".corner-two"))));
			}
			if (configSec.contains("requirement.requires")) {
				return new QuestObjectiveTripwire(
						tripwires,
						getQuestItems(configSec.getConfigurationSection("requirement.requires")),
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
						Material.getMaterial(configSec.getString("requirement.block-type").toUpperCase()),
						(configSec.contains("requirement.amount") ? configSec.getInt("requirement.amount") : null),
						(configSec.contains("requirement.location") ? parseLocation(configSec.getString("requirement.location")) : null),
						getQuestItems(configSec.getConfigurationSection("requirement.requires")),
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			} else {
				return new QuestObjectiveBreak(
						Material.getMaterial(configSec.getString("requirement.block-type").toUpperCase()),
						(configSec.contains("requirement.amount") ? configSec.getInt("requirement.amount") : null),
						(configSec.contains("requirement.location") ? parseLocation(configSec.getString("requirement.location")) : null),
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			}
		}
		return null;
	}

	public static QuestFirstNpc loadFirstNpc(ConfigurationSection configSec, int objectivesNumber) {
		return new QuestFirstNpc(
				configSec.getInt("npc-id"),
				getStringList(configSec, "speech"),
				(configSec.contains("idle-messages") ? loadIdleMessages(configSec.getConfigurationSection("idle-messages"), objectivesNumber) : null),
				(configSec.contains("quest-completed-message") ? getStringList(configSec, "quest-completed-message") : null),
				configSec.getString("npc-name"),
				(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
				configSec.getBoolean("deniable"),
				(configSec.getBoolean("deniable") ? getStringList(configSec, "denied-message") : null),
				(configSec.getBoolean("deniable") ? getStringList(configSec, "accepted-message") : null));
	}

	public static QuestNpc loadNpc(ConfigurationSection configSec, int objectivesNumber) {
		return new QuestNpc(
				configSec.getInt("npc-id"),
				getStringList(configSec, "speech"),
				(configSec.contains("idle-messages") ? loadIdleMessages(configSec.getConfigurationSection("idle-messages"), objectivesNumber) : null),
				configSec.getString("npc-name"));
	}

	public static List<QuestItem> getQuestItems(ConfigurationSection configSec) {
		List<QuestItem> questItems = new ArrayList<QuestItem>();
		for (String key : configSec.getKeys(false)) {
			questItems.add(new QuestItem(
					configSec.getString(key + ".item-name"),
					configSec.getString(key + ".item-type"),
					configSec.getInt(key + ".item-count")));
		}
		return questItems;
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
		World world = Bukkit.getWorld(str.split(",")[0].replaceAll(",", ""));
		double x = Double.parseDouble(str.split(",")[1].replaceAll(",", ""));
		double y = Double.parseDouble(str.split(",")[2].replaceAll(",", ""));
		double z = Double.parseDouble(str.split(",")[3].replaceAll(",", ""));
		return new Location(world, x, y, z);
	}

}
