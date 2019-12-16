package com.runicrealms.runicquests.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

import com.runicrealms.runicquests.quests.FirstNpcState;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.objective.QuestObjective;

public class PlayerDataLoader {
	
	// This allows us to not need to load the player data for each player every time they log in, instead we can cache it
	private static HashMap<UUID, DataFileConfiguration> cachedPlayerData = new HashMap<UUID, DataFileConfiguration>();

	// Parses quest data for a user. This is very confusing code, but should not need to be changed.
	public static List<Quest> getQuestDataForUser(UUID uuid, String characterSlot) {
		List<Quest> quests = QuestLoader.getBlankQuestList();
		List<Quest> newQuests = new ArrayList<Quest>();
		DataFileConfiguration runicFileConfig = getConfigFromCache(uuid);
		ConfigurationSection data = runicFileConfig.getConfig().get(characterSlot);
		if (data != null) {
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

	// Loads a config from cache. If it has not been cached, then load it.
	public static DataFileConfiguration getConfigFromCache(UUID uuid) {
		if (!cachedPlayerData.containsKey(uuid)) {
			cachedPlayerData.put(uuid, new DataFileConfiguration(uuid));
		}
		return cachedPlayerData.get(uuid);
	}
	
	public static HashMap<UUID, DataFileConfiguration> getCachedPlayerData() {
		return cachedPlayerData;
	}

}
