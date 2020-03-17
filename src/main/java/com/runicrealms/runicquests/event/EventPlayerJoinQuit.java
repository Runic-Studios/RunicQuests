package com.runicrealms.runicquests.event;

import java.util.*;

import com.runicrealms.plugin.utilities.FloatingItemUtil;
import com.runicrealms.runiccharacters.api.events.CharacterLoadEvent;
import com.runicrealms.runicquests.config.QuestLoader;
import com.runicrealms.runicquests.task.TaskQueue;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runiccharacters.api.events.CharacterQuitEvent;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.config.PlayerDataLoader;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EventPlayerJoinQuit implements Listener {

	@EventHandler
	public void onPlayerJoin(CharacterLoadEvent event) {
		runJoinEvent(event.getPlayer(), event.getUserConfig().getCharacterSlot());
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
			Plugin.getQuestProfiles().remove(questProfile);
		}
		if (PlayerDataLoader.getCachedPlayerData().containsKey(event.getPlayer().getUniqueId())) {
			PlayerDataLoader.getCachedPlayerData().remove(event.getPlayer().getUniqueId());
		}
		if (Plugin.getCachedLocations().containsKey(event.getPlayer())) {
			Plugin.getCachedLocations().remove(event.getPlayer());
		}
	}
	
	public static void runJoinEvent(Player player, Integer characterSlot) {
		Map<Integer, Set<Integer>> cooldowns = new HashMap<Integer, Set<Integer>>();
		for (int i = 1; i <= RunicCharactersApi.getAllCharacters(player.getUniqueId()).size(); i++) {
			cooldowns.put(i, new HashSet<>());
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

	/**
	 * Displays question marks above quest giver's heads!
	 */
	public static void displayQuestionMarks() {
		final int DURATION = 10;
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player pl : Bukkit.getOnlinePlayers()) {
					if (Plugin.getQuestProfile(pl.getUniqueId().toString()) == null) continue;
					//QuestProfile questProfile = Plugin.getQuestProfile(pl.getUniqueId().toString());
					for (Quest quest : QuestLoader.getBlankQuestList()) { // Loop through quests to find a match for the NPC
						//if (questProfile.getQuests().contains(quest)) continue;
						// todo: add quest.isRepeatable()
						// todo:
						NPC firstNPC = quest.getFirstNPC().getCitizensNpc();
						if (!firstNPC.isSpawned()) continue; // prevent glitched NPCs
						Location loc = firstNPC.getStoredLocation();
						FloatingItemUtil.createFloatingItem(loc.add(0, 2.5, 0), Material.ORANGE_DYE, DURATION);
					}
				}
			}
		}.runTaskTimer(Plugin.getInstance(), 100L, DURATION*20L);
	}
}
