package com.runicrealms.player;

import java.util.List;

import com.runicrealms.config.PlayerDataLoader;
import com.runicrealms.quests.Quest;

public class QuestProfile {
	
	public List<Quest> quests;
	public String playerUUID;
	
	public QuestProfile(String uuid) {
		this.playerUUID = uuid;
		quests = PlayerDataLoader.getQuestDataForUser(uuid);
	}
	
	public void saveProgress() {
		
	}
	
}
