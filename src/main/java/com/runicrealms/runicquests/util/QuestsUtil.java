package com.runicrealms.runicquests.util;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.model.QuestProfileData;
import com.runicrealms.runicquests.quests.Quest;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class QuestsUtil {
    public static final String PREFIX = "&6[Quest] »";

    /**
     * Utility method to check if a player can start a repeatable quest
     *
     * @param uuid  of the given player
     * @param quest the given quest
     * @return true if the player can start the repeatable quest
     */
    public static boolean canStartRepeatableQuest(UUID uuid, Quest quest) {
        Map<UUID, Map<Integer, Date>> cooldowns = RunicQuests.getQuestCooldowns();
        int questId = quest.getQuestID();
        // If there are no in-memory CDs for quest, can start
        if (cooldowns.get(uuid) == null || !cooldowns.get(uuid).containsKey(questId)) {
            return true;
        }
        // If there is an in-memory CD for the quest, check its remaining time
        long differenceInSeconds = repeatableQuestTimeElapsed(uuid, questId);
        if (differenceInSeconds >= quest.getCooldown()) {
            cooldowns.get(uuid).remove(questId);
            return true;
        }
        return false;
    }

    /**
     * Determines the amount of time (in seconds) that have passed since player last
     * finished a repeatable quest
     *
     * @param uuid    of the player
     * @param questId of the quest
     * @return the time elapsed (in seconds)
     */
    public static long repeatableQuestTimeElapsed(UUID uuid, int questId) {
        Date currentTime = new Date();
        Date questCompleteTime = RunicQuests.getQuestCooldowns().get(uuid).get(questId);
        return (currentTime.getTime() - questCompleteTime.getTime()) / 1000;
    }

    /**
     * Returns a string that represents the time remaining before the player can complete a repeatable quest
     *
     * @param player to lookup
     * @param quest  the repeatable quest
     * @return a string version of the time remaining
     */
    public static String repeatableQuestTimeRemaining(Player player, Quest quest) {
        long timeElapsed = repeatableQuestTimeElapsed(player.getUniqueId(), quest.getQuestID());
        long differenceInSeconds = quest.getCooldown() - timeElapsed;

        long hours = differenceInSeconds / 3600;
        long minutes = (differenceInSeconds % 3600) / 60;
        long seconds = differenceInSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if (hours > 0) {
            sb.append(hours).append("h, ");
        }

        if (minutes > 0) {
            sb.append(minutes).append("m, ");
        }

        sb.append(seconds).append("s");

        return sb.toString();
    }

    /**
     * Calculates the total quest points for player based on their completed quests
     *
     * @param uuid of the player
     * @return their quest points
     */
    public static int calculateQuestPoints(UUID uuid) {
        int result = 0;
        QuestProfileData profileData = RunicQuests.getAPI().getQuestProfile(uuid);
        int slot = RunicCore.getCharacterAPI().getCharacterSlot(uuid);
        for (Quest quest : profileData.getQuestsMap().get(slot)) {
            if (!quest.getQuestState().isCompleted()) continue;
            result += quest.getRewards().getQuestPointsReward();
        }
        return result;
    }
}
