package com.runicrealms.runicquests.api;

import java.util.List;

import com.runicrealms.runicquests.config.QuestProfile;
import org.bukkit.entity.Player;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.config.QuestLoader;
import com.runicrealms.runicquests.quests.Quest;

public class RunicQuestsAPI {

	public static QuestProfile getQuestProfile(Player player) {
		return Plugin.getQuestProfile(player.getUniqueId().toString());
	}
	
	public static List<Quest> getBlankQuestList() {
		return QuestLoader.getBlankQuestList();
	}
	
}
