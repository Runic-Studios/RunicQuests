package com.runicrealms.api;

import java.util.List;

import org.bukkit.entity.Player;

import com.runicrealms.Plugin;
import com.runicrealms.config.QuestLoader;
import com.runicrealms.player.QuestProfile;
import com.runicrealms.quests.Quest;

public class RunicQuestsAPI {

	public static QuestProfile getQuestProfile(Player player) {
		return Plugin.getQuestProfile(player.getUniqueId().toString());
	}
	
	public static List<Quest> getUnusedQuestList() {
		return QuestLoader.getBlankQuestList();
	}
	
}
