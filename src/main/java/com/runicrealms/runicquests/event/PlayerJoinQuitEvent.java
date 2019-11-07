package com.runicrealms.runicquests.event;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;

public class PlayerJoinQuitEvent implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		runJoinEvent(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Plugin.getQuestCooldowns().remove(event.getPlayer().getUniqueId().toString()); // Remove the cooldown object
		QuestProfile questProfile = Plugin.getQuestProfile(event.getPlayer().getUniqueId().toString()); // Get the quest profile
		for (Quest quest : questProfile.getQuests()) { // Loop through the quests
			for (QuestObjective objective : quest.getObjectives()) { // Loop through objectives
				if (objective.getObjectiveType() == QuestObjectiveType.TALK) { // Check for objective of type talk
					/**
					 * This is a minor bug fix which prevents minor issues with players
					 * talking to NPCs, then logging out
					 */
					if (Plugin.getNpcTaskQueues().containsKey(((QuestObjectiveTalk) objective).getQuestNpc().getId())) {
						Plugin.getNpcTaskQueues().remove(((QuestObjectiveTalk) objective).getQuestNpc().getId());
					}
				}
			}
		}
		if (!Plugin.CACHE_PLAYER_DATA) { // If we aren't caching profiles, remove it
			Plugin.getQuestProfiles().remove(Plugin.getQuestProfiles().indexOf(questProfile));
		}
	}
	
	public static void runJoinEvent(Player player) {
		Plugin.getQuestCooldowns().put(player.getUniqueId().toString(), new ArrayList<Integer>()); // Add a cooldown to the list of cooldowns
		if (Plugin.CACHE_PLAYER_DATA) { // Whether or not to cache player data
			for (QuestProfile profile : Plugin.getQuestProfiles()) { // Loop through quest profiles
				if (profile.getPlayerUUID().equalsIgnoreCase(player.getUniqueId().toString())) { // If there is a cached profile, we can exit
					return;
				}
			}
			Plugin.getQuestProfiles().add(new QuestProfile(player.getUniqueId().toString())); // If there isn't a cached profile, add one
		} else {
			Plugin.getQuestProfiles().add(new QuestProfile(player.getUniqueId().toString())); // Because we aren't caching profiles, add one
		}
	}

}
