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
					quests.remove(quests.indexOf(quest));
					newQuest.getQuestState().setCompleted(data.getConfigurationSection(quest.getQuestID() + "").getBoolean("completed"));
					newQuest.getQuestState().setStarted(data.getConfigurationSection(quest.getQuestID() + "").getBoolean("started"));
					newQuest.getFirstNPC().setState(FirstNpcState.fromString(data.getConfigurationSection(quest.getQuestID() + "").getString("first-npc-state")));
					for (String objectiveNumber : data.getConfigurationSection(quest.getQuestID() + "").getConfigurationSection("objectives").getKeys(false)) {
						for (QuestObjective questObjective : quest.getObjectives().keySet()) {
							if (objectiveNumber.equalsIgnoreCase(questObjective.getObjectiveNumber() + "")) {
								newQuest.getObjectives().get(questObjective).setCompleted(data.getConfigurationSection(quest.getQuestID() + "").getBoolean("objectives." + objectiveNumber));
							}
						}
					}
					newQuests.add(newQuest);
				}
			}
		}
		List<Quest> missingQuests = new ArrayList<Quest>();
		for (Quest newQuest : newQuests) {
			boolean containsUnprocessedQuest = false;
			for (Quest unprocessedQuest : quests) {
				if (newQuest.getQuestID() == unprocessedQuest.getQuestID()) {
					containsUnprocessedQuest = true;
					break;
				}
				if (containsUnprocessedQuest == false) {
					missingQuests.add(unprocessedQuest);
				}
			}
		}
		for (Quest quest : missingQuests) {
			newQuests.add(quest);
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
