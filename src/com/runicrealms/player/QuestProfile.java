package com.runicrealms.player;

import java.util.List;

import com.runicrealms.config.DataFileConfiguration;
import com.runicrealms.config.PlayerDataLoader;
import com.runicrealms.quests.Quest;

public class QuestProfile {
	
	public List<Quest> quests;
	public String playerUUID;
	public DataFileConfiguration savedData;
	
	public QuestProfile(String uuid) {
		this.playerUUID = uuid;
		this.quests = PlayerDataLoader.getQuestDataForUser(uuid);
		this.savedData = PlayerDataLoader.getConfigFromCache(uuid);
	}
	
}
