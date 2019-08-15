package com.runicrealms.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.runicrealms.Plugin;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestObjective;
import com.runicrealms.quests.QuestObjectiveType;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class NpcClickEvent implements Listener {

	@EventHandler
	public void onNpcRightClick(NPCRightClickEvent event) {
		for (Quest quest : Plugin.getQuestProfile(event.getClicker().getUniqueId().toString()).quests) {
			if (quest.state.started == false && quest.state.completed == false) {
				if (quest.firstNPC.npc.getId() == event.getNPC().getId()) {
					if (QuestObjective.getFirstObjective(quest.objectives).completed == false) {
						// TODO
					}
				}
			} else if (quest.state.started) {
				for (QuestObjective objective : quest.objectives.keySet()) {
					if (objective.completed == false) {
						if (objective.objectiveNumber != 1) {
							if (QuestObjective.getLastObjective(quest.objectives, objective.objectiveNumber).completed == false) {
								return;
							}
						}
						if (objective.objectiveType == QuestObjectiveType.TALK) {
							if (objective.questNpc.npc.getId() == event.getNPC().getId()) {
								// TODO
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onNpcLeftClick(NPCLeftClickEvent event) {
		for (Quest quest : Plugin.getQuestProfile(event.getClicker().getUniqueId().toString()).quests) {
			if (quest.state.started == false && quest.state.completed == false) {
				if (quest.firstNPC.npc.getId() == event.getNPC().getId()) {
					if (QuestObjective.getFirstObjective(quest.objectives).completed == false) {
						// TODO
					}
				}
			} else if (quest.state.started) {
				for (QuestObjective objective : quest.objectives.keySet()) {
					if (objective.completed == false) {
						if (objective.objectiveNumber != 1) {
							if (QuestObjective.getLastObjective(quest.objectives, objective.objectiveNumber).completed == false) {
								return;
							}
						}
						if (objective.objectiveType == QuestObjectiveType.TALK) {
							if (objective.questNpc.npc.getId() == event.getNPC().getId()) {
								// TODO
							}
						}
					}
				}
			}
		}
	}

}
