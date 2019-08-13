package com.runicrealms.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.runicrealms.Plugin;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestItem;
import com.runicrealms.quests.QuestNpc;
import com.runicrealms.quests.QuestObjective;
import com.runicrealms.quests.QuestRewards;

public class QuestLoader {
	
	public static Quest loadQuest(ConfigurationSection configSec) {
		List<QuestObjective> objectives = new ArrayList<QuestObjective>();
		for (int i = 0; i < configSec.getKeys(false).size(); i++) {
			objectives.add(loadObjective(configSec.getConfigurationSection(i + "")));
		}
		return new Quest(
				configSec.getString("name"),
				loadNpc(configSec.getConfigurationSection("firstNPC")),
				objectives,
				loadRewards(configSec.getConfigurationSection("rewards")));
	}
	
	public static QuestRewards loadRewards(ConfigurationSection configSec) {
		List<String> execute = new ArrayList<String>();
		if (configSec.isString("execute")) {
			execute.add(configSec.getString("execute"));
		} else {
			execute = configSec.getStringList("execute");
		}
		return new QuestRewards(
				configSec.getInt("exp"),
				configSec.getInt("quest-points"),
				configSec.getInt("money"),
				execute);
	}

	public static QuestObjective loadObjective(ConfigurationSection configSec) {
		List<String> goalMessage = new ArrayList<String>();
		if (configSec.isString("goal-message")) {
			goalMessage.add(configSec.getString("goal-message"));
		} else {
			goalMessage = configSec.getStringList("goal-message");
		}
		if (configSec.getString("requirement.type").equalsIgnoreCase("slay")) {
			if (configSec.contains("requirement.requires")) {
				return new QuestObjective(
						configSec.getString("requirement.mob-name"), 
						configSec.getInt("requirement.amount"), 
						new QuestItem(configSec.getString("requirement.requires.item-name"), configSec.getString("requirement.requires.item-type")), 
						goalMessage);
			} else {
				return new QuestObjective(
						configSec.getString("requirement.mob-name"), 
						configSec.getInt("requirement.amount"), 
						goalMessage);
			}
		} else if (configSec.getString("requirement.type").equalsIgnoreCase("talk")) {
			if (configSec.contains("requirement.requires")) {
				return new QuestObjective(
						loadNpc(configSec.getConfigurationSection("requirement.npc")),
						new QuestItem(configSec.getString("requirement.requires.item-name"), configSec.getString("requirement.requires.item-type")), 
						goalMessage);
			} else {
				return new QuestObjective(
						loadNpc(configSec.getConfigurationSection("requirement.npc")),
						goalMessage);
			}
		} else if (configSec.getString("requirement.type").equalsIgnoreCase("tripwire")) {
			if (configSec.contains("requirement.requires")) {
				double x1 = Double.parseDouble(configSec.getString("requirement.tripwire1").split(",")[0].replaceAll(",", ""));
				double y1 = Double.parseDouble(configSec.getString("requirement.tripwire1").split(",")[1].replaceAll(",", ""));
				double z1 = Double.parseDouble(configSec.getString("requirement.tripwire1").split(",")[2].replaceAll(",", ""));
				double x2 = Double.parseDouble(configSec.getString("requirement.tripwire2").split(",")[0].replaceAll(",", ""));
				double y2 = Double.parseDouble(configSec.getString("requirement.tripwire2").split(",")[1].replaceAll(",", ""));
				double z2 = Double.parseDouble(configSec.getString("requirement.tripwire2").split(",")[2].replaceAll(",", ""));
				return new QuestObjective(
						new Location(Bukkit.getWorld(Plugin.WORLD_NAME), x1, y1, z1),
						new Location(Bukkit.getWorld(Plugin.WORLD_NAME), x2, y2, z2),
						new QuestItem(configSec.getString("requirement.requires.item-name"), configSec.getString("requirement.requires.item-type")), 
						goalMessage);
			} else {
				double x1 = Double.parseDouble(configSec.getString("requirement.tripwire1").split(",")[0].replaceAll(",", ""));
				double y1 = Double.parseDouble(configSec.getString("requirement.tripwire1").split(",")[1].replaceAll(",", ""));
				double z1 = Double.parseDouble(configSec.getString("requirement.tripwire1").split(",")[2].replaceAll(",", ""));
				double x2 = Double.parseDouble(configSec.getString("requirement.tripwire2").split(",")[0].replaceAll(",", ""));
				double y2 = Double.parseDouble(configSec.getString("requirement.tripwire2").split(",")[1].replaceAll(",", ""));
				double z2 = Double.parseDouble(configSec.getString("requirement.tripwire2").split(",")[2].replaceAll(",", ""));
				return new QuestObjective(
						new Location(Bukkit.getWorld(Plugin.WORLD_NAME), x1, y1, z1),
						new Location(Bukkit.getWorld(Plugin.WORLD_NAME), x2, y2, z2),
						goalMessage);
			}
		} else if (configSec.getString("requirement.type").equalsIgnoreCase("break")) {
			if (configSec.contains("requirement.requires")) {
				double x = Double.parseDouble(configSec.getString("requirement.block-location").split(",")[0].replaceAll(",", ""));
				double y = Double.parseDouble(configSec.getString("requirement.block-location").split(",")[1].replaceAll(",", ""));
				double z = Double.parseDouble(configSec.getString("requirement.block-location").split(",")[2].replaceAll(",", ""));
				return new QuestObjective(
						Material.getMaterial(configSec.getString("requirement.block-type").toUpperCase()),
						new Location(Bukkit.getWorld(Plugin.WORLD_NAME), x, y, z),
						new QuestItem(configSec.getString("requirement.requires.item-name"), configSec.getString("requirement.requires.item-type")), 
						goalMessage);
			} else {
				double x = Double.parseDouble(configSec.getString("requirement.block-location").split(",")[0].replaceAll(",", ""));
				double y = Double.parseDouble(configSec.getString("requirement.block-location").split(",")[1].replaceAll(",", ""));
				double z = Double.parseDouble(configSec.getString("requirement.block-location").split(",")[2].replaceAll(",", ""));
				return new QuestObjective(
						Material.getMaterial(configSec.getString("requirement.block-type").toUpperCase()),
						new Location(Bukkit.getWorld(Plugin.WORLD_NAME), x, y, z),
						goalMessage);
			}
		}
		return null;
	}

	public static QuestNpc loadNpc(ConfigurationSection configSec) {
		List<String> speech = new ArrayList<String>();
		List<String> idleMessage = new ArrayList<String>();
		List<String> completedMessage = new ArrayList<String>();
		List<String> execute = new ArrayList<String>();
		if (configSec.isString("speech")) {
			speech.add(configSec.getString("speech"));
		} else {
			speech = configSec.getStringList("speech");
		}
		if (configSec.contains("idle-message")) {
			if (configSec.isString("idle-message")) {
				idleMessage.add(configSec.getString("idle-message"));
			} else {
				idleMessage = configSec.getStringList("idle-message");
			}
		} else {
			idleMessage = null;
		}
		if (configSec.isString("completed-message")) {
			speech.add(configSec.getString("completed-message"));
		} else {
			speech = configSec.getStringList("completed-message");
		}
		if (configSec.contains("execute")) {
			if (configSec.isString("execute")) {
				idleMessage.add(configSec.getString("execute"));
			} else {
				idleMessage = configSec.getStringList("execute");
			}
		} else {
			idleMessage = null;
		}
		return new QuestNpc(configSec.getInt("npc-id"), speech, idleMessage, completedMessage, execute);
	}

}
