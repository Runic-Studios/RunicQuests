package com.runicrealms.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.runicrealms.Plugin;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestObjective;

public class ConfigLoader {

	public static FileConfiguration getYamlConfigFile(String fileName) {
		FileConfiguration config;
		File file;
		file = new File(Plugin.getInstance().getDataFolder(), fileName);
		config = new YamlConfiguration();
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			config.load(file);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return config;
	}

	public static List<Quest> getQuestDataForUser(String uuid) {
		File folder = getSubFolder(Plugin.getInstance().getDataFolder(), "users");
		List<Quest> quests = getQuestList();
		for (File file : folder.listFiles()) {
			if (file.getName().equalsIgnoreCase(uuid + ".yml")) {
				FileConfiguration data = getYamlConfigFile(file.getName());
				for (Quest quest : quests) {
					if (quest.questName.equalsIgnoreCase(data.getString("name"))) {
						Quest newQuest = new Quest(quest);
						quests.remove(quests.indexOf(quest));
						newQuest.state.completed = data.getBoolean("completed");
						newQuest.state.started = data.getBoolean("started");
						for (String objectiveNumber : data.getConfigurationSection("objectives").getKeys(false)) {
							for (QuestObjective questObjective : quest.objectives.keySet()) {
								if (objectiveNumber.equalsIgnoreCase(questObjective.objectiveNumber + "")) {
									newQuest.objectives.get(questObjective).completed = 
											data.getBoolean("objectives." + objectiveNumber + ".completed");
								}
							}
						}
						quests.add(newQuest);
					}
				}
			}
		}
		return quests;
	}

	public static File getSubFolder(File folder, String subfolder) {
		for (File file : folder.listFiles()) {
			if (file.getName().equalsIgnoreCase(subfolder)) {
				return file;
			}
		}
		return null;
	}

	public static List<Quest> getQuestList() {
		List<Quest> quests = new ArrayList<Quest>();
		File folder = getSubFolder(Plugin.getInstance().getDataFolder(), "quests");
		for (File quest : folder.listFiles()) {
			quests.add(QuestLoader.loadQuest(getYamlConfigFile(quest.getName())));
		}
		return quests;
	}

}
