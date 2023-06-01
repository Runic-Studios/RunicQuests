package com.runicrealms.runicquests.quests;

import org.bukkit.Bukkit;

import java.util.List;
import java.util.Map;

/**
 * Represents quest rewards
 */
public class QuestRewards {

    private final Integer exp;
    private final Integer questPoints;
    private final Integer money;
    private final List<String> execute;
    private final Map<String, Integer> items; // String is template id, integer is count

    public QuestRewards(Integer exp, Integer questPoints, Integer money, List<String> execute, Map<String, Integer> items) {
        this.exp = exp;
        this.questPoints = questPoints;
        this.money = money;
        this.execute = execute;
        this.items = items;
    }

    public Integer getExperienceReward() {
        return this.exp;
    }

    public Integer getQuestPointsReward() {
        return this.questPoints;
    }

    public Integer getMoneyReward() {
        return this.money;
    }

    public boolean hasExecute() {
        return this.execute != null;
    }

    public void executeCommand(String playerName) {
        for (String command : this.execute) {
            String parsedCommand = command.startsWith("/") ? command.substring(1).replaceAll("%player%", playerName) : command.replaceAll("%player%", playerName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
        }
    }

    public Map<String, Integer> getItems() {
        return this.items;
    }

}
