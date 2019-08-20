package com.runicrealms.player;

import java.util.List;

import com.runicrealms.config.DataFileConfiguration;
import com.runicrealms.config.PlayerDataLoader;
import com.runicrealms.quests.FirstNpcState;
import com.runicrealms.quests.Quest;

public class QuestProfile {
	
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
	
	public void dumpFirstNpcStates() {
		for (Quest quest : this.quests) { // TODO - save NPC deny on restart
			if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) {
				quest.getFirstNPC().setState(FirstNpcState.NEUTRAL);
			}
		}
		this.save();
	}

}
