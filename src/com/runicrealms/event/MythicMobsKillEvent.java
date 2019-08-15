package com.runicrealms.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.runicrealms.Plugin;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestObjective;
import com.runicrealms.quests.QuestObjectiveType;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;

public class MythicMobsKillEvent implements Listener {

	@EventHandler
	public void onKill(MythicMobDeathEvent event) {
		if (event.getKiller() instanceof Player) {
			for (Quest quest : Plugin.getQuestProfile(((Player) event.getKiller()).getUniqueId().toString()).quests) {
				if (quest.state.completed == false) {
					for (QuestObjective objective : quest.objectives.keySet()) {
						if (quest.objectives.get(objective).completed == false) {
							if (objective.objectiveNumber != 1) {
								if (QuestObjective.getLastObjective(quest.objectives, objective.objectiveNumber).completed == false) {
									return;
								}
							}
							if (objective.objectiveType == QuestObjectiveType.SLAY) {
								if (event.getMob().getType().getInternalName().equalsIgnoreCase(objective.mobName)) {
									// TODO
								}
							}
						}
					}
				}
			}
		}
	}

}