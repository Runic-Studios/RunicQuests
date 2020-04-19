package com.runicrealms.runicquests.event;

import java.util.*;

import com.runicrealms.runiccharacters.api.events.CharacterLoadEvent;
import com.runicrealms.runiccharacters.api.events.CharacterQuitEvent;
import com.runicrealms.runicquests.data.QuestProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;

public class EventPlayerJoinQuit implements Listener {

	@EventHandler
	public void onPlayerJoin(CharacterLoadEvent event) {
		runJoinEvent(event.getPlayer(), event.getCharacterWrapper().getCharacterSlot());
	}

	@EventHandler
	public void onPlayerQuit(CharacterQuitEvent event) {
		runQuitEvent(event.getPlayer());
	}

	public static void runJoinEvent(Player player, Integer characterSlot) {
		Map<Integer, Set<Integer>> cooldowns = new HashMap<Integer, Set<Integer>>();
		for (int i = 1; i <= RunicCharactersApi.getAllCharacters(player.getUniqueId()).size(); i++) {
			cooldowns.put(i, new HashSet<>());
		}
		Plugin.getQuestCooldowns().put(player.getUniqueId(), cooldowns); // Add a cooldown to the list of cooldowns
		PlayerDataLoader.addPlayerQuestData(player.getUniqueId(), characterSlot, () -> Plugin.updatePlayerCachedLocations(player)); // Add a quest profile
	}

	public static void runQuitEvent(Player player) {
		Plugin.getQuestCooldowns().remove(player.getUniqueId()); // Remove the cooldown object
		QuestProfile questProfile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId()); // Get the quest profile
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
		PlayerDataLoader.getAllPlayerData().remove(player.getUniqueId());
		if (Plugin.getCachedLocations().containsKey(player.getUniqueId())) {
			Plugin.getCachedLocations().remove(player.getUniqueId());
		}
	}

	/**
	 * Displays question marks above quest giver's heads!
	 */
	public static void displayQuestionMarks() {
//		final int DURATION = 10;
//		new BukkitRunnable() {
//			@Override
//			public void run() {
//				for (Player pl : Bukkit.getOnlinePlayers()) {
//					QuestProfile profile = Plugin.getQuestProfile(pl.getUniqueId().toString());
//					if (profile == null) continue;
//					for (Quest quest : profile.getQuests()) { // Loop through quests to find a match for the NPC
//						NPC firstNPC = quest.getFirstNPC().getCitizensNpc();
//						if (!firstNPC.isSpawned()) continue; // prevent glitched NPCs
//						Location loc = firstNPC.getStoredLocation();
//						Material mark = Material.ORANGE_DYE;
//						if (quest.isSideQuest()) mark = Material.DANDELION_YELLOW;
//						if (quest.isRepeatable()) mark = Material.LIGHT_BLUE_DYE;
//						if (quest.getQuestState().isCompleted()) mark = Material.CACTUS_GREEN;
//						FloatingItemUtil.spawnFloatingItem(pl, loc.add(0, 2.5, 0), mark, DURATION);
//					}
//				}
//			}
//		}.runTaskTimer(Plugin.getInstance(), 100L, (DURATION-1)*20L);
	}

	public static void refreshPlayerData(QuestProfile profile, Player player) {
		Plugin.getQuestCooldowns().remove(player.getUniqueId());
		QuestProfile questProfile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId());
		for (Quest quest : questProfile.getQuests()) {
			for (QuestObjective objective : quest.getObjectives()) {
				if (objective.getObjectiveType() == QuestObjectiveType.TALK) {
					if (Plugin.getNpcTaskQueues().containsKey(((QuestObjectiveTalk) objective).getQuestNpc().getId())) {
						Plugin.getNpcTaskQueues().remove(((QuestObjectiveTalk) objective).getQuestNpc().getId());
					}
				}
			}
		}
		Plugin.getCachedLocations().remove(player.getUniqueId());
		Map<Integer, Set<Integer>> cooldowns = new HashMap<Integer, Set<Integer>>();
		for (int i = 1; i <= RunicCharactersApi.getAllCharacters(player.getUniqueId()).size(); i++) {
			cooldowns.put(i, new HashSet<>());
		}
		Plugin.getQuestCooldowns().put(player.getUniqueId(), cooldowns);
		Plugin.updatePlayerCachedLocations(player);
	}
}