package com.runicrealms.runicquests.player;

import java.util.List;
import java.util.UUID;

import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runicquests.config.DataFileConfiguration;
import com.runicrealms.runicquests.config.PlayerDataLoader;
import com.runicrealms.runicquests.quests.Quest;

public class QuestProfile {
	
	/*
	 * This class is meant to bind a DataConfiguration file (which binds File+FileConfiguration), 
	 * a player UUID, and a List<Quest> (which already exists in the DataConfigurationFile)
	 */
	
	private List<Quest> quests;
	private UUID playerUUID;
	private DataFileConfiguration savedData;
	private Integer characterSlot;
	
	public QuestProfile(UUID uuid, Integer characterSlot) {
		this.playerUUID = uuid;
		this.quests = PlayerDataLoader.getQuestDataForUser(uuid, characterSlot);
		this.savedData = PlayerDataLoader.getConfigFromCache(uuid);
		this.characterSlot = characterSlot;
	}
	
	public void save() {
		this.savedData.saveToConfig(this.quests, RunicCharactersApi.getCurrentCharacterSlot(this.playerUUID));
	}

	public List<Quest> getQuests() {
		return quests;
	}

	public UUID getPlayerUUID() {
		return playerUUID;
	}

	public DataFileConfiguration getSavedData() {
		return savedData;
	}
	
	public Integer getCharacterSlot() {
		return this.characterSlot;
	}
	
}
