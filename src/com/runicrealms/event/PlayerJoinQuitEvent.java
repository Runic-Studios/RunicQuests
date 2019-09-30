package com.runicrealms.event;

import java.util.ArrayList;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.runicrealms.Plugin;
import com.runicrealms.player.QuestProfile;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestObjective;
import com.runicrealms.quests.QuestObjectiveType;

public class PlayerJoinQuitEvent implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Plugin.cooldowns.put(event.getPlayer().getUniqueId().toString(), new ArrayList<Integer>());
		if (Plugin.CACHE_PLAYER_DATA) {
			for (QuestProfile profile : Plugin.getQuestProfiles()) {
				if (profile.getPlayerUUID().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
					return;
				}
			}
			Plugin.getQuestProfiles().add(new QuestProfile(event.getPlayer().getUniqueId().toString()));
		} else {
			Plugin.getQuestProfiles().add(new QuestProfile(event.getPlayer().getUniqueId().toString()));
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Plugin.cooldowns.remove(event.getPlayer().getUniqueId().toString());
		QuestProfile questProfile = Plugin.getQuestProfile(event.getPlayer().getUniqueId().toString());
		for (Quest quest : questProfile.getQuests()) {
			for (QuestObjective objective : quest.getObjectives()) {
				if (objective.getObjectiveType() == QuestObjectiveType.TALK) {
					if (NpcClickEvent.npcs.containsKey(objective.getQuestNpc().getId())) {
						NpcClickEvent.npcs.remove(objective.getQuestNpc().getId());
					}
				}
			}
		}
		if (!Plugin.CACHE_PLAYER_DATA) {
			Plugin.getQuestProfiles().remove(Plugin.getQuestProfiles().indexOf(questProfile));
		}
	}

}
