package com.runicrealms.player;

import java.util.List;

import com.runicrealms.config.DataFileConfiguration;
import com.runicrealms.config.PlayerDataLoader;
import com.runicrealms.quests.Quest;

public class QuestProfile {
	
	/*
	 * This class is meant to bind a DataConfiguration file (which binds File+FileConfiguration), 
	 * a player UUID, and a List<Quest> (which already exists in the DataConfigurationFile)
	 */
	
	private List<Quest> quests;
	private String playerUUID;
	private DataFileConfiguration savedData;
	
	public QuestProfile(String uuid) {
		this.playerUUID = uuid;
		this.quests = PlayerDataLoader.getQuestDataForUser(uuid);
		this.savedData = PlayerDataLoader.getConfigFromCache(uuid);
	}
	
	public void save() {
		this.savedData.saveToConfig(this.quests);
	}

	public List<Quest> getQuests() {
		return quests;
	}

	public String getPlayerUUID() {
		return playerUUID;
	}

	public DataFileConfiguration getSavedData() {
		return savedData;
	}
	
}
