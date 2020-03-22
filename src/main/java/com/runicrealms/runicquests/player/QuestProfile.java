package com.runicrealms.runicquests.player;

import java.util.Collections;
import java.util.Comparator;
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
		Collections.sort(this.quests, new Comparator<Quest>() {
			@Override
			public int compare(Quest a, Quest b) {
				if (a.getRequirements().getClassLvReq() > b.getRequirements().getClassLvReq()) {
					return 1;
				} else if (a.getRequirements().getClassLvReq() < b.getRequirements().getClassLvReq()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		this.savedData = PlayerDataLoader.getConfigFromCache(uuid);
		this.characterSlot = characterSlot;
	}
	
	public void save() {
		this.savedData.saveToConfig(this.quests, RunicCharactersApi.getCurrentCharacterSlot(this.playerUUID));
	}

	public List<Quest> getQuests() {
		return quests;
	}

	/*
	Returns an object from getQuests to manipulate a player's quest object by name
	 */
	public Quest getPlayerQuest(String name) {
		return quests.stream().filter(q -> q.getQuestName().equals(name)).findFirst().orElse(null);
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
