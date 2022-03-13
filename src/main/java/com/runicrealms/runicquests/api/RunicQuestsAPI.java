package com.runicrealms.runicquests.api;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.config.QuestLoader;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.ui.QuestMenu;
import org.bukkit.entity.Player;

import java.util.List;

public class RunicQuestsAPI {

    /**
     * Returns a string that represents the time remaining before the player can complete a repeatable quest
     *
     * @param player  to lookup
     * @param questId the id of the repeatable quest
     * @return a string version of the time remaining
     */
    public static String repeatableQuestTimeRemaining(Player player, int questId) {
        int cooldownLeft = (int) Math.floor((Plugin.getQuestCooldowns().get(player.getUniqueId()).get(questId) - System.currentTimeMillis()) / 1000.0);
        int hours = (cooldownLeft - (cooldownLeft % 3600)) / 3600; // Some very odd code to create a cooldown message
        int minutes = (cooldownLeft - (cooldownLeft % 60)) / 60 - (hours * 60);
        int seconds = cooldownLeft - (hours * 3600) - (minutes * 60);
        return (hours == 0 ? "" : hours + " " + (hours == 1 ? "hour, " : "hours, ")) +
                (minutes == 0 ? "" : minutes + " " + (seconds == 0 ? (minutes == 1 ? "minute" : "minutes") : (minutes == 1 ? "minute, " : "minutes, "))) +
                (seconds == 0 ? "" : seconds + " " + (seconds == 1 ? "second" : "seconds"));
    }

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
        player.openInventory(new QuestMenu(player).getInventory());
    }

}
