package com.runicrealms.runicquests.api;

import java.util.List;

import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import org.bukkit.entity.Player;

import com.runicrealms.runicquests.config.QuestLoader;
import com.runicrealms.runicquests.quests.Quest;

public class RunicQuestsAPI {

	public static QuestProfile getQuestProfile(Player player) {
		return PlayerDataLoader.getPlayerQuestData(player.getUniqueId());
	}
	
	public static List<Quest> getBlankQuestList() {
		return QuestLoader.getBlankQuestList();
	}
	
}
