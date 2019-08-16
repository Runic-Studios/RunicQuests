package com.runicrealms.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.runicrealms.Plugin;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestObjective;
import com.runicrealms.quests.QuestObjectiveType;

public class PlayerBreakBlockEvent implements Listener {

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		for (Quest quest : Plugin.getQuestProfile(event.getPlayer().getUniqueId().toString()).getQuests()) {
			if (quest.getQuestState().isCompleted() == false) {
				for (QuestObjective objective : quest.getObjectives().keySet()) {
					if (quest.getObjectives().get(objective).isCompleted() == false) {
						if (objective.getObjectiveNumber() != 1) {
							if (QuestObjective.getLastObjective(quest.getObjectives(), objective.getObjectiveNumber()).isCompleted() == false) {
								return;
							}
						}
						if (objective.getObjectiveType() == QuestObjectiveType.BREAK) {
							if (objective.getBlockMaterial() == event.getBlock().getType()) {
								// TODO
							}
						}
					}
				}
			}
		}
	}

}
