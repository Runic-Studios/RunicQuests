package com.runicrealms.player;

import java.util.List;

import com.runicrealms.config.DataFileConfiguration;
import com.runicrealms.config.PlayerDataLoader;
import com.runicrealms.quests.Quest;

public class QuestProfile {
	
	private List<Quest> quests;
	private String playerUUID;
	private DataFileConfiguration savedData;
	
	public QuestProfile(String uuid) {
		this.setPlayerUUID(uuid);
		this.quests = PlayerDataLoader.getQuestDataForUser(uuid);
		this.setSavedData(PlayerDataLoader.getConfigFromCache(uuid));
	}

	public List<Quest> getQuests() {
		return quests;
	}

	public String getPlayerUUID() {
		return playerUUID;
	}

	public void setPlayerUUID(String playerUUID) {
		this.playerUUID = playerUUID;
	}

	public DataFileConfiguration getSavedData() {
		return savedData;
	}

	public void setSavedData(DataFileConfiguration savedData) {
		this.savedData = savedData;
	}

}
