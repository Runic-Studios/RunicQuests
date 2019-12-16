package com.runicrealms.runicquests.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import org.bukkit.entity.Player;
import runicrealms.runiccharacters.RunicCharacters;
import runicrealms.runiccharacters.api.RunicCharactersApi;

public class DataFileConfiguration {
	
	/*
	 * This class is meant to bound a File and a ConfigurationSection
	 */
	
	private HashMap<String, ConfigurationSection> config = new HashMap<String, ConfigurationSection>();
	private UUID uuid;
	
	public DataFileConfiguration(UUID uuid) {
		for (String characterSlot : RunicCharactersApi.getAllCharacters(uuid)) {
			if (!RunicCharactersApi.hasDataForKey(uuid, Integer.parseInt(characterSlot), "quests")) {
				RunicCharactersApi.set(uuid, Integer.parseInt(characterSlot), "quests", "temp", 0);
			}
			this.config.put(characterSlot, RunicCharactersApi.getData(uuid, Integer.parseInt(characterSlot), "quests"));
		}
		this.uuid = uuid;
	}
	
	// Writes a List<Quest> to the ConfigurationSection, then writes the ConfigurationSection to user data
	public void saveToConfig(List<Quest> quests, int characterSlot) {
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
	public HashMap<String, ConfigurationSection> getConfig() {
		return this.config;
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
}
