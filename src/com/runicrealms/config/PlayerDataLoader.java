package com.runicrealms.config;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import com.runicrealms.Plugin;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestObjective;

public class PlayerDataLoader {
	
	public static List<Quest> getQuestDataForUser(String uuid) {
		File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "users");
		List<Quest> quests = QuestLoader.getBlankQuestList();
		for (File file : folder.listFiles()) {
			if (file.getName().equalsIgnoreCase(uuid + ".yml")) {
				FileConfiguration data = ConfigLoader.getYamlConfigFile(file.getName());
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
	
}
