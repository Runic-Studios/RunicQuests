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
		for (Quest quest : Plugin.getQuestProfile(event.getPlayer().getUniqueId().toString()).quests) {
			if (quest.state.completed == false) {
				for (QuestObjective objective : quest.objectives.keySet()) {
					if (quest.objectives.get(objective).completed == false) {
						if (objective.objectiveNumber != 1) {
							if (QuestObjective.getLastObjective(quest.objectives, objective.objectiveNumber).completed == false) {
								return;
							}
						}
						if (objective.objectiveType == QuestObjectiveType.BREAK) {
							if (objective.blockMaterial == event.getBlock().getType()) {
								// TODO
							}
						}
					}
				}
			}
		}
	}

}
