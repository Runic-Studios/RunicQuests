package com.runicrealms.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.runicrealms.Plugin;
import com.runicrealms.quests.CraftingProfessionType;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestFirstNpc;
import com.runicrealms.quests.QuestItem;
import com.runicrealms.quests.QuestNpc;
import com.runicrealms.quests.QuestObjective;
import com.runicrealms.quests.QuestRequirements;
import com.runicrealms.quests.QuestRewards;

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
		for (int i = 1; i <= config.getConfigurationSection("objectives").getKeys(false).size(); i++) {
			objectives.add(loadObjective(config.getConfigurationSection("objectives." + i), i));
		}
		return new Quest(
				config.getString("name"),
				loadFirstNpc(config.getConfigurationSection("first-npc")),
				objectives,
				loadRewards(config.getConfigurationSection("rewards")),
				config.getInt("unique-id"),
				loadRequirements(config.getConfigurationSection("requirements")),
				config.getBoolean("side-quest"),
				config.getBoolean("repeatable"),
				(config.contains("completion-speech") ? getStringList(config, "completion-speech") : null),
				(config.contains("use-last-npc-name-for-completion-speech") ? config.getBoolean("use-last-npc-name-for-completion-speech") : null),
				(config.getBoolean("repeatable") ? config.getInt("quest-cooldown") : null));
	}

	public static QuestRequirements loadRequirements(ConfigurationSection configSec) {
		Integer levelReq = configSec.getInt("level");
		List<String> levelNotMet = getStringList(configSec, "level-not-met");
		Integer craftingReq = null;
		List<String> craftingNotMet = null;
		CraftingProfessionType professionType = null;
		if (configSec.contains("tailoring-level")) {
			craftingNotMet = getStringList(configSec, "crafting-level-not-met");
			craftingReq = configSec.getInt("tailoring-level");
			professionType = CraftingProfessionType.TAILORING;
		} else if (configSec.contains("blacksmithing-level")) {
			craftingNotMet = getStringList(configSec, "crafting-level-not-met");
			craftingReq = configSec.getInt("blacksmithing-level");
			professionType = CraftingProfessionType.BLACKSMITHING;
		} else if (configSec.contains("leatherworking-level")) {
			craftingNotMet = getStringList(configSec, "crafting-level-not-met");
			craftingReq = configSec.getInt("leatherworking-level");
			professionType = CraftingProfessionType.LEATHERWORKING;
		} else if (configSec.contains("jeweling-level")) {
			craftingNotMet = getStringList(configSec, "crafting-level-not-met");
			craftingReq = configSec.getInt("jeweling-level");
			professionType = CraftingProfessionType.JEWELING;
		} else if (configSec.contains("alchemy-level")) {
			craftingNotMet = getStringList(configSec, "crafting-level-not-met");
			craftingReq = configSec.getInt("alchemy-level");
			professionType = CraftingProfessionType.ALCHEMY;
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
		return new QuestRequirements(levelReq, craftingReq, professionType, requiredQuests, levelNotMet, craftingNotMet, requiredQuestsNotMet);
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

	public static QuestObjective loadObjective(ConfigurationSection configSec, Integer objectiveNumber) {
		List<String> goalMessage = new ArrayList<String>();
		if (configSec.isString("goal-message")) {
			goalMessage.add(configSec.getString("goal-message"));
		} else {
			goalMessage = configSec.getStringList("goal-message");
		}
		if (configSec.getString("requirement.type").equalsIgnoreCase("slay")) {
			if (configSec.contains("requirement.requires")) {
				return new QuestObjective(
						getStringList(configSec, "requirement.mob-names"), 
						configSec.getInt("requirement.amount"), 
						getQuestItems(configSec.getConfigurationSection("requirement.requires")),
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			} else {
				return new QuestObjective(
						getStringList(configSec, "requirement.mob-names"), 
						configSec.getInt("requirement.amount"), 
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			}
		} else if (configSec.getString("requirement.type").equalsIgnoreCase("talk")) {
			if (configSec.contains("requirement.requires")) {
				return new QuestObjective(
						loadNpc(configSec.getConfigurationSection("requirement.npc")),
						getQuestItems(configSec.getConfigurationSection("requirement.requires")),
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			} else {
				return new QuestObjective(
						loadNpc(configSec.getConfigurationSection("requirement.npc")),
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			}
		} else if (configSec.getString("requirement.type").equalsIgnoreCase("tripwire")) {
			if (configSec.contains("requirement.requires")) {
				double x1 = Double.parseDouble(configSec.getString("requirement.tripwire-one").split(",")[0].replaceAll(",", ""));
				double y1 = Double.parseDouble(configSec.getString("requirement.tripwire-one").split(",")[1].replaceAll(",", ""));
				double z1 = Double.parseDouble(configSec.getString("requirement.tripwire-one").split(",")[2].replaceAll(",", ""));
				double x2 = Double.parseDouble(configSec.getString("requirement.tripwire-two").split(",")[0].replaceAll(",", ""));
				double y2 = Double.parseDouble(configSec.getString("requirement.tripwire-two").split(",")[1].replaceAll(",", ""));
				double z2 = Double.parseDouble(configSec.getString("requirement.tripwire-two").split(",")[2].replaceAll(",", ""));
				return new QuestObjective(
						new Location(Bukkit.getWorld(Plugin.WORLD_NAME), x1, y1, z1),
						new Location(Bukkit.getWorld(Plugin.WORLD_NAME), x2, y2, z2),
						getQuestItems(configSec.getConfigurationSection("requirement.requires")),
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			} else {
				double x1 = Double.parseDouble(configSec.getString("requirement.tripwire-one").split(",")[0].replaceAll(",", ""));
				double y1 = Double.parseDouble(configSec.getString("requirement.tripwire-one").split(",")[1].replaceAll(",", ""));
				double z1 = Double.parseDouble(configSec.getString("requirement.tripwire-one").split(",")[2].replaceAll(",", ""));
				double x2 = Double.parseDouble(configSec.getString("requirement.tripwire-two").split(",")[0].replaceAll(",", ""));
				double y2 = Double.parseDouble(configSec.getString("requirement.tripwire-two").split(",")[1].replaceAll(",", ""));
				double z2 = Double.parseDouble(configSec.getString("requirement.tripwire-two").split(",")[2].replaceAll(",", ""));
				return new QuestObjective(
						new Location(Bukkit.getWorld(Plugin.WORLD_NAME), x1, y1, z1),
						new Location(Bukkit.getWorld(Plugin.WORLD_NAME), x2, y2, z2),
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			}
		} else if (configSec.getString("requirement.type").equalsIgnoreCase("break")) {
			if (configSec.contains("requirement.requires")) {
				return new QuestObjective(
						Material.getMaterial(configSec.getString("requirement.block-type").toUpperCase()),
						getQuestItems(configSec.getConfigurationSection("requirement.requires")),
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			} else {
				return new QuestObjective(
						Material.getMaterial(configSec.getString("requirement.block-type").toUpperCase()),
						goalMessage,
						(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
						objectiveNumber,
						(configSec.contains("completed-message") ? getStringList(configSec, "completed-message") : null));
			}
		}
		return null;
	}

	public static QuestFirstNpc loadFirstNpc(ConfigurationSection configSec) {
		return new QuestFirstNpc(
				configSec.getInt("npc-id"),
				getStringList(configSec, "speech"),
				(configSec.contains("idle-message") ? getStringList(configSec, "idle-message") : null),
				(configSec.contains("quest-completed-message") ? getStringList(configSec, "quest-completed-message") : null),
				configSec.getString("npc-name"),
				(configSec.contains("execute") ? getStringList(configSec, "execute") : null),
				configSec.getBoolean("deniable"),
				(configSec.getBoolean("deniable") ? getStringList(configSec, "denied-message") : null),
				(configSec.getBoolean("deniable") ? getStringList(configSec, "accepted-message") : null));
	}

	public static QuestNpc loadNpc(ConfigurationSection configSec) {
		return new QuestNpc(
				configSec.getInt("npc-id"),
				getStringList(configSec, "speech"),
				(configSec.contains("idle-message") ? getStringList(configSec, "idle-message") : null),
				(configSec.contains("quest-completed-message") ? getStringList(configSec, "quest-completed-message") : null),
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

}
