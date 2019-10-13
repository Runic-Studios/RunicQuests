package com.runicrealms.event;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.runicrealms.Plugin;
import com.runicrealms.player.QuestProfile;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestObjectiveType;
import com.runicrealms.quests.objective.QuestObjective;
import com.runicrealms.quests.objective.QuestObjectiveTalk;

public class PlayerJoinQuitEvent implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		runJoinEvent(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Plugin.getQuestCooldowns().remove(event.getPlayer().getUniqueId().toString());
		QuestProfile questProfile = Plugin.getQuestProfile(event.getPlayer().getUniqueId().toString());
		for (Quest quest : questProfile.getQuests()) {
			for (QuestObjective objective : quest.getObjectives()) {
				if (objective.getObjectiveType() == QuestObjectiveType.TALK) {
					if (Plugin.getNpcTaskQueues().containsKey(((QuestObjectiveTalk) objective).getQuestNpc().getId())) {
						Plugin.getNpcTaskQueues().remove(((QuestObjectiveTalk) objective).getQuestNpc().getId());
					}
				}
			}
		}
		if (!Plugin.CACHE_PLAYER_DATA) {
			Plugin.getQuestProfiles().remove(Plugin.getQuestProfiles().indexOf(questProfile));
		}
	}
	
	public static void runJoinEvent(Player player) {
		Plugin.getQuestCooldowns().put(player.getUniqueId().toString(), new ArrayList<Integer>());
		if (Plugin.CACHE_PLAYER_DATA) {
			for (QuestProfile profile : Plugin.getQuestProfiles()) {
				if (profile.getPlayerUUID().equalsIgnoreCase(player.getUniqueId().toString())) {
					return;
				}
			}
			Plugin.getQuestProfiles().add(new QuestProfile(player.getUniqueId().toString()));
		} else {
			Plugin.getQuestProfiles().add(new QuestProfile(player.getUniqueId().toString()));
		}
	}

}
