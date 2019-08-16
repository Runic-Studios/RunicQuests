package com.runicrealms.config;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import com.runicrealms.Plugin;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestObjective;

public class PlayerDataLoader {

	private static HashMap<String, DataFileConfiguration> cachedPlayerData = new HashMap<String, DataFileConfiguration>();

	public static void initDirs() {
		File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "users");
		if (folder == null) {
			folder = new File(Plugin.getInstance().getDataFolder(), "users");
			folder.mkdir();
		}
		folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "quests");
		if (folder == null) {
			folder = new File(Plugin.getInstance().getDataFolder(), "quests");
			folder.mkdir();
		}
	}

	public static List<Quest> getQuestDataForUser(String uuid) {
		List<Quest> quests = QuestLoader.getBlankQuestList();
		DataFileConfiguration runicFileConfig = getConfigFromCache(uuid);
		FileConfiguration data = runicFileConfig.getConfig();
		for (Quest quest : quests) {
			for (String dataQuestID : data.getKeys(false)) {
				if (dataQuestID.equalsIgnoreCase(quest.getQuestID() + "")) {
					Quest newQuest = new Quest(quest);
					quests.remove(quests.indexOf(quest));
					newQuest.getQuestState().setCompleted(data.getBoolean("completed"));
					newQuest.getQuestState().setStarted(data.getBoolean("started"));
					for (String objectiveNumber : data.getConfigurationSection("objectives").getKeys(false)) {
						for (QuestObjective questObjective : quest.getObjectives().keySet()) {
							if (objectiveNumber.equalsIgnoreCase(questObjective.getObjectiveNumber() + "")) {
								newQuest.getObjectives().get(questObjective).setCompleted(data.getBoolean("objectives." + objectiveNumber + ".completed"));
							}
						}
					}
					quests.add(newQuest);
				}
			}
		}
		return quests;
	}

	public static DataFileConfiguration getConfigFromCache(String uuid) {
		if (!cachedPlayerData.containsKey(uuid)) {
			cachedPlayerData.put(uuid, DataFileConfiguration.getFile(uuid + ".yml"));
		}
		return cachedPlayerData.get(uuid);
	}

}
