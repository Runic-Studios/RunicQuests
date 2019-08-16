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
			for (Quest quest : Plugin.getQuestProfile(((Player) event.getKiller()).getUniqueId().toString()).getQuests()) {
				if (quest.getQuestState().isCompleted() == false) {
					for (QuestObjective objective : quest.getObjectives().keySet()) {
						if (quest.getObjectives().get(objective).isCompleted() == false) {
							if (objective.getObjectiveNumber() != 1) {
								if (QuestObjective.getLastObjective(quest.getObjectives(), objective.getObjectiveNumber()).isCompleted() == false) {
									return;
								}
							}
							if (objective.getObjectiveType() == QuestObjectiveType.SLAY) {
								if (event.getMob().getType().getInternalName().equalsIgnoreCase(objective.getMobName())) {
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