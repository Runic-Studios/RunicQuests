package com.runicrealms.runicquests.config;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.objective.QuestObjective;

public class DataFileConfiguration {
	
	/*
	 * This class is meant to bound a File and a ConfigurationSection
	 */
	
	private HashMap<Integer, ConfigurationSection> config = new HashMap<Integer, ConfigurationSection>();
	private UUID uuid;
	
	public DataFileConfiguration(UUID uuid) {
		for (int i = 1; i <= RunicCharactersApi.getAllCharacters(uuid).size(); i++) {
			if (!RunicCharactersApi.hasDataForKey(uuid, i, "quests")) {
				RunicCharactersApi.set(uuid, i, "quests", "temp", 0);
			}
			this.config.put(i, RunicCharactersApi.getData(uuid, i, "quests"));
		}
		this.uuid = uuid;
	}
	
	// Writes a List<Quest> to the ConfigurationSection, then writes the ConfigurationSection to user data
	public void saveToConfig(List<Quest> quests, Integer characterSlot) {
		for (Quest quest : quests) {
			config.get(characterSlot).set(quest.getQuestID() + ".started", quest.getQuestState().hasStarted());
			config.get(characterSlot).set(quest.getQuestID() + ".completed", quest.getQuestState().isCompleted());
			config.get(characterSlot).set(quest.getQuestID() + ".first-npc-state", quest.getFirstNPC().getState().getName());
			for (QuestObjective objective : quest.getObjectives()) {
				config.get(characterSlot).set(quest.getQuestID() + ".objectives." + objective.getObjectiveNumber() + "", objective.isCompleted());
			}
		}
		RunicCharactersApi.set(uuid, characterSlot, "quests", config.get(characterSlot));
	}

	// Returns the configurationSection
	public HashMap<Integer, ConfigurationSection> getConfig() {
		return this.config;
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
}
