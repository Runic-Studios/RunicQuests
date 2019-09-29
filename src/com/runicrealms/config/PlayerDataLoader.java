package com.runicrealms.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import com.runicrealms.quests.FirstNpcState;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestObjective;

public class PlayerDataLoader {

	private static HashMap<String, DataFileConfiguration> cachedPlayerData = new HashMap<String, DataFileConfiguration>();

	public static List<Quest> getQuestDataForUser(String uuid) {
		List<Quest> quests = QuestLoader.getBlankQuestList();
		List<Quest> newQuests = new ArrayList<Quest>();
		DataFileConfiguration runicFileConfig = getConfigFromCache(uuid);
		FileConfiguration data = runicFileConfig.getConfig();
		for (Quest quest : quests) {
			for (String dataQuestID : data.getKeys(false)) {
				if (dataQuestID.equalsIgnoreCase(quest.getQuestID() + "")) {
					Quest newQuest = new Quest(quest);
					
					newQuest.getQuestState().setCompleted(data.getConfigurationSection(quest.getQuestID() + "").getBoolean("completed"));
					newQuest.getQuestState().setStarted(data.getConfigurationSection(quest.getQuestID() + "").getBoolean("started"));
					newQuest.getFirstNPC().setState(FirstNpcState.fromString(data.getConfigurationSection(quest.getQuestID() + "").getString("first-npc-state")));
					for (String objectiveNumber : data.getConfigurationSection(quest.getQuestID() + "").getConfigurationSection("objectives").getKeys(false)) {
						for (QuestObjective questObjective : quest.getObjectives()) {
							if (objectiveNumber.equalsIgnoreCase(questObjective.getObjectiveNumber() + "")) {
								questObjective.setCompleted(data.getConfigurationSection(quest.getQuestID() + "").getBoolean("objectives." + objectiveNumber));
							}
						}
					}
					newQuests.add(newQuest);
				}
			}
		}
		for (Quest unloadedQuest : quests) {
			boolean containsQuest = false;
			for (Quest newQuest : newQuests) {
				if (unloadedQuest.getQuestID() == newQuest.getQuestID()) {
					containsQuest = true;
					break;
				}
			}
			if (containsQuest == false) {
				newQuests.add(unloadedQuest);
			}
		}
		return newQuests;
	}

	public static DataFileConfiguration getConfigFromCache(String uuid) {
		if (!cachedPlayerData.containsKey(uuid)) {
			cachedPlayerData.put(uuid, DataFileConfiguration.getFile(uuid + ".yml"));
		}
		return cachedPlayerData.get(uuid);
	}

}
