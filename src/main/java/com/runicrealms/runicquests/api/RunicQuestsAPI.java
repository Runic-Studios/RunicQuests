package com.runicrealms.runicquests.api;

import java.util.List;

import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.event.EventInventory;
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

	/**
	 * Opens the quest GUI on the default page for the given player
	 *
	 * @param player to shop quest menu to
	 */
	public static void openQuestGui(Player player) {
		EventInventory.openQuestGui(player, 1);
	}
	
}
