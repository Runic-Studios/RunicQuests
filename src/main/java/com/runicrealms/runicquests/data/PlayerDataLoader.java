package com.runicrealms.runicquests.data;

import java.util.HashMap;
import java.util.UUID;

public class PlayerDataLoader {
	
	public static HashMap<UUID, QuestProfile> playerData = new HashMap<>();

	public static QuestProfile getPlayerQuestData(UUID uuid) {
		return playerData.get(uuid);
	}

	public static QuestProfile addPlayerQuestData(UUID uuid, Integer slot, Runnable onCompletion) {
		QuestProfile profile = new QuestProfile(uuid.toString(), slot, onCompletion);
		playerData.put(uuid, profile);
		return profile;
	}

	public static HashMap<UUID, QuestProfile> getAllPlayerData() {
		return playerData;
	}

}
