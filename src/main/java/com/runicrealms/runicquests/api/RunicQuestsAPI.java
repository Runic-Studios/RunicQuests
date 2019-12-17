package com.runicrealms.runicquests.api;

import java.util.List;

import org.bukkit.entity.Player;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.config.QuestLoader;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;

public class RunicQuestsAPI {

	public static QuestProfile getQuestProfile(Player player) {
		return Plugin.getQuestProfile(player.getUniqueId().toString());
	}
	
	public static List<Quest> getUnusedQuestList() {
		return QuestLoader.getBlankQuestList();
	}
	
}