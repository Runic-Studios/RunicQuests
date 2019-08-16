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
		for (Quest quest : Plugin.getQuestProfile(event.getClicker().getUniqueId().toString()).getQuests()) {
			if (quest.getQuestState().hasStarted() == false && quest.getQuestState().isCompleted() == false) {
				if (quest.getFirstNPC().getCitizensNpc().getId() == event.getNPC().getId()) {
					if (QuestObjective.getFirstObjective(quest.getObjectives()).isCompleted() == false) {
						// TODO
					}
				}
			} else if (quest.getQuestState().hasStarted()) {
				for (QuestObjective objective : quest.getObjectives().keySet()) {
					if (objective.isCompleted() == false) {
						if (objective.getObjectiveNumber() != 1) {
							if (QuestObjective.getLastObjective(quest.getObjectives(), objective.getObjectiveNumber()).isCompleted() == false) {
								return;
							}
						}
						if (objective.getObjectiveType() == QuestObjectiveType.TALK) {
							if (objective.getQuestNpc().getCitizensNpc().getId() == event.getNPC().getId()) {
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
		for (Quest quest : Plugin.getQuestProfile(event.getClicker().getUniqueId().toString()).getQuests()) {
			if (quest.getQuestState().hasStarted() == false && quest.getQuestState().isCompleted() == false) {
				if (quest.getFirstNPC().getCitizensNpc().getId() == event.getNPC().getId()) {
					if (QuestObjective.getFirstObjective(quest.getObjectives()).isCompleted() == false) {
						// TODO
					}
				}
			} else if (quest.getQuestState().hasStarted()) {
				for (QuestObjective objective : quest.getObjectives().keySet()) {
					if (objective.isCompleted() == false) {
						if (objective.getObjectiveNumber() != 1) {
							if (QuestObjective.getLastObjective(quest.getObjectives(), objective.getObjectiveNumber()).isCompleted() == false) {
								return;
							}
						}
						if (objective.getObjectiveType() == QuestObjectiveType.TALK) {
							if (objective.getQuestNpc().getCitizensNpc().getId() == event.getNPC().getId()) {
								// TODO
							}
						}
					}
				}
			}
		}
	}

}
