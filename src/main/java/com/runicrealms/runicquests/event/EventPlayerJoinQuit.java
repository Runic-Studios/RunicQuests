package com.runicrealms.runicquests.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runiccharacters.api.events.CharacterEnterEvent;
import com.runicrealms.runiccharacters.api.events.CharacterQuitEvent;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.config.PlayerDataLoader;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;

public class EventPlayerJoinQuit implements Listener {

	@EventHandler
	public void onPlayerJoin(CharacterEnterEvent event) {
		runJoinEvent(event.getPlayer(), event.getCharacter().getSlot());
	}

	@EventHandler
	public void onPlayerQuit(CharacterQuitEvent event) {
		Plugin.getQuestCooldowns().remove(event.getPlayer().getUniqueId()); // Remove the cooldown object
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
		if (PlayerDataLoader.getCachedPlayerData().containsKey(event.getPlayer().getUniqueId())) {
			PlayerDataLoader.getCachedPlayerData().remove(event.getPlayer().getUniqueId());
		}
		if (Plugin.getCachedLocations().containsKey(event.getPlayer())) {
			Plugin.getCachedLocations().remove(event.getPlayer());
		}
	}
	
	public static void runJoinEvent(Player player, Integer characterSlot) {
		Map<Integer, List<Integer>> cooldowns = new HashMap<Integer, List<Integer>>();
		for (int i = 1; i <= RunicCharactersApi.getAllCharacters(player.getUniqueId()).size(); i++) {
			cooldowns.put(i, new ArrayList<Integer>());
		}
		Plugin.getQuestCooldowns().put(player.getUniqueId(), cooldowns); // Add a cooldown to the list of cooldowns
		if (Plugin.CACHE_PLAYER_DATA) { // Whether or not to cache player data
			for (QuestProfile profile : Plugin.getQuestProfiles()) { // Loop through quest profiles
				if (profile.getPlayerUUID().toString().equalsIgnoreCase(player.getUniqueId().toString())) { // If there is a cached profile, we can exit
					return;
				}
			}
			Plugin.getQuestProfiles().add(new QuestProfile(player.getUniqueId(), characterSlot)); // If there isn't a cached profile, add one
		} else {
			Plugin.getQuestProfiles().add(new QuestProfile(player.getUniqueId(), characterSlot)); // Because we aren't caching profiles, add one
		}
	}

}
