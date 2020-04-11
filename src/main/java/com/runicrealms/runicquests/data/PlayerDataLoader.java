package com.runicrealms.runicquests.data;

import java.util.HashMap;
import java.util.UUID;

public class PlayerDataLoader {
	
	public static HashMap<UUID, QuestProfile> playerData = new HashMap<UUID, QuestProfile>();

	public static QuestProfile getPlayerQuestData(UUID uuid) {
		return playerData.get(uuid);
	}

	public static QuestProfile addPlayerQuestData(UUID uuid, Integer slot) {
		QuestProfile profile = new QuestProfile(uuid.toString(), slot);
		playerData.put(uuid, profile);
		return profile;
	}

	public static HashMap<UUID, QuestProfile> getAllPlayerData() {
		return playerData;
	}

}
