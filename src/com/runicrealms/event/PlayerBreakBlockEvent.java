package com.runicrealms.event;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.runicrealms.Plugin;
import com.runicrealms.player.QuestProfile;
import com.runicrealms.quests.FirstNpcState;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestObjective;
import com.runicrealms.quests.QuestObjectiveType;

public class PlayerBreakBlockEvent implements Listener {

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		QuestProfile questProfile = Plugin.getQuestProfile(player.getUniqueId().toString());
		for (Quest quest : questProfile.getQuests()) {
			if (quest.getQuestState().isCompleted() == false && quest.getQuestState().hasStarted()) {
				for (QuestObjective objective : quest.getObjectives().keySet()) {
					if (quest.getObjectives().get(objective).isCompleted() == false) {
						if (objective.getObjectiveNumber() != 1) {
							if (QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 1).isCompleted() == false) {
								return;
							}
						}
						if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) {
							return;
						}
						if (objective.requiresQuestItem()) {
							boolean hasQuestItem = false;
							for (ItemStack item : player.getInventory().getContents()) {
								if (ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase(objective.getQuestItem().getItemName())) {
									if (item.getType() == Material.getMaterial(objective.getQuestItem().getItemType())) {
										hasQuestItem = true;
										break;
									}
								}
							}
							if (!hasQuestItem) { 
								return;
							}
						}
						if (objective.getObjectiveType() == QuestObjectiveType.BREAK) {
							if (objective.getBlockMaterial() == event.getBlock().getType()) {
								objective.setCompleted(true);
								questProfile.save();
								if (objective.hasExecute()) {
									objective.executeCommand(player.getName());
								}
								if (objective.hasCompletedMessage()) {
									for (String message : objective.getCompletedMessage()) {
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
									}
								}
								if (objective.getObjectiveNumber() != QuestObjective.getLastObjective(quest.getObjectives()).getObjectiveNumber()) {
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l&6New objective for: &r&l&e") + quest.getQuestName());
									for (String message : QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() + 1).getGoalMessage()) {
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- &r&6" + message));
									}
								} else {
									quest.getQuestState().setCompleted(true);
									questProfile.save();
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lRewards:"));
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getQuestPointsReward() + " &r&aQuest Point" + (quest.getRewards().getQuestPointsReward() > 1 ? "s" : "")));
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getMoneyReward() + " &r&aCoin" + (quest.getRewards().getMoneyReward() > 1 ? "s" : "")));
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a- &r" + quest.getRewards().getExperienceReward() + " &r&aExperience"));
									if (quest.getRewards().hasExecute()) {
										quest.getRewards().executeCommand(player.getName());
									}
								}
							}
						}
					}
				}
			}
		}
	}

}
