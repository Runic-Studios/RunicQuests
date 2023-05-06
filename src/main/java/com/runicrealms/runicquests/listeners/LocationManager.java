package com.runicrealms.runicquests.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.model.QuestProfileData;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.location.LocationToReach;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveHandler;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveLocation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LocationManager implements Listener, QuestObjectiveHandler {
    /*
     * This Map is meant to help with performance issues with checking the player's location. It will just indicate
     * when a player has a location objective on one of their quests
     */
    private static final Map<Player, Map<Integer, LocationToReach>> cachedLocations = new ConcurrentHashMap<>();

    public LocationManager() {
        for (UUID uuid : RunicCore.getCharacterAPI().getLoadedCharacters()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            updatePlayerCachedLocations(player);
        }
        startLocationTask();
    }

    public Map<Player, Map<Integer, LocationToReach>> getCachedLocations() {
        return cachedLocations;
    }

    /**
     * Used to progress objectives which require a location to be reached
     *
     * @param player  that reached the location
     * @param slot    of their character
     * @param questID id of quest with matching location objective
     */
    private void progressLocationObjective(Player player, int slot, Integer questID) {
        QuestProfileData profileData = RunicQuests.getAPI().getQuestProfile(player.getUniqueId());
        Quest questFound = null;
        for (Quest q : profileData.getQuestsMap().get(slot)) {
            if (q.getQuestID().equals(questID)) {
                questFound = q;
            }
        }
        if (questFound == null) return;
        final Quest quest = new Quest(questFound);
        if (!isQuestActive(quest)) return;
        for (QuestObjective objective : quest.getObjectives()) {
            if (!isValidObjective(quest, objective, QuestObjectiveType.LOCATION)) continue;
            if (!questItemRequirementMet(player, objective)) continue;
            progressQuest(player, profileData, quest, objective);
            return; // Return to prevent fallthrough if quest has multiple location objectives back-to-back
        }
    }

    /**
     * Schedule a task that will run for the cached location objectives.
     * This prevents us from needing to constantly check the player's location for location objectives
     */
    private void startLocationTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(RunicQuests.getInstance(), () -> {
            for (Map.Entry<Player, Map<Integer, LocationToReach>> entry : cachedLocations.entrySet()) {
                for (Map.Entry<Integer, LocationToReach> questLocationToReach : entry.getValue().entrySet()) {
                    if (questLocationToReach.getValue().hasReachedLocation(entry.getKey())) {
                        int slot = RunicCore.getCharacterAPI().getCharacterSlot(entry.getKey().getUniqueId());
                        progressLocationObjective(entry.getKey(), slot, questLocationToReach.getKey());
                        updatePlayerCachedLocations(entry.getKey());
                        break;
                    }
                }
            }
        }, 10L, 10L);
    }

    /**
     * Updates the cached location objectives for a player (performance reasons)
     *
     * @param player to update locations for
     * @param slot   of the character
     */
    public void updatePlayerCachedLocations(Player player, int slot) {
        cachedLocations.put(player, new HashMap<>());
        /*
        This can cause server crashes if we try to read this from mongo or redis. Best to keep it to the in-memory cache here
         */
        QuestProfileData profileData = (QuestProfileData) RunicQuests.getAPI().getSessionDataMap().get(player.getUniqueId());
        if (profileData == null) return;
        if (profileData.getQuestsMap() == null) return;
        if (profileData.getQuestsMap().get(slot) == null) return;
        for (Quest quest : profileData.getQuestsMap().get(slot)) {
            for (QuestObjective objective : quest.getObjectives()) {
                if (!isValidObjective(quest, objective, QuestObjectiveType.LOCATION)) continue;
                cachedLocations.get(player).put(quest.getQuestID(), ((QuestObjectiveLocation) objective).getLocation());
            }
        }
        if (cachedLocations.get(player).size() == 0) {
            cachedLocations.remove(player);
        }
    }

    public void updatePlayerCachedLocations(Player player) {
        int slot = RunicCore.getCharacterAPI().getCharacterSlot(player.getUniqueId());
        updatePlayerCachedLocations(player, slot);
    }
}
