package com.runicrealms.runicquests.command;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.objective.QuestObjective;

public class QuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player == false) {
			sender.sendMessage(ChatColor.RED + "Only players can use this!");
			return true;
		}
		// Loops through player's quests, checks for last objective state + quest status
		Player player = (Player) sender;
		HashMap<String, String> goalMessages = new HashMap<String, String>();
		for (Quest quest : Plugin.getQuestProfile(player.getUniqueId().toString()).getQuests()) {
			QuestObjective lowestUncompletedObjective = null;
			for (QuestObjective objective : quest.getObjectives()) {
				if (objective.isCompleted() == false && (objective.getObjectiveNumber() < lowestUncompletedObjective.getObjectiveNumber() || lowestUncompletedObjective == null)) {
					lowestUncompletedObjective = objective;
				}
			}
			if (lowestUncompletedObjective != null) {
				goalMessages.put(quest.getQuestName(), lowestUncompletedObjective.getGoalMessage());
			}
		}
		player.sendMessage(ChatColor.GOLD + "Current Objectives:");
		for (String questName : goalMessages.keySet()) {
			player.sendMessage(ChatColor.GOLD + questName + ": " + ChatColor.YELLOW + goalMessages.get(questName));
		}
		return true;
	}

}
