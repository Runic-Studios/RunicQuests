package com.runicrealms.event;

import org.apache.commons.lang.math.IntRange;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.runicrealms.Plugin;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestObjective;
import com.runicrealms.quests.QuestObjectiveType;

public class PlayerTripwireEvent implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL) {
			if (event.getClickedBlock().getType() == Material.TRIPWIRE ||
					event.getClickedBlock().getType() == Material.TRIPWIRE_HOOK) {
				for (Quest quest : Plugin.getQuestProfile(event.getPlayer().getUniqueId().toString()).getQuests()) {
					if (quest.getQuestState().isCompleted() == false) {
						for (QuestObjective objective : quest.getObjectives().keySet()) {
							if (objective.getObjectiveNumber() != 1) {
								if (QuestObjective.getLastObjective(quest.getObjectives(), objective.getObjectiveNumber()).isCompleted() == false) {
									return;
								}
							}
							if (objective.getObjectiveType() == QuestObjectiveType.TRIPWIRE) {
								if (new IntRange(objective.getTripwire1().getBlockX(), objective.getTripwire2().getBlockX()).containsInteger(event.getClickedBlock().getX()) &&
										new IntRange(objective.getTripwire1().getBlockY(), objective.getTripwire2().getBlockY()).containsInteger(event.getClickedBlock().getY())
										new IntRange(objective.getTripwire1().getBlockZ(), objective.getTripwire2().getBlockZ()).containsInteger(event.getClickedBlock().getZ())) {
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
