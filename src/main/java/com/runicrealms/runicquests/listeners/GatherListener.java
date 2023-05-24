package com.runicrealms.runicquests.listeners;

import com.runicrealms.plugin.professions.event.GatheringEvent;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.model.QuestProfileData;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveGather;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveHandler;
import com.runicrealms.runicquests.util.QuestsUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GatherListener implements Listener, QuestObjectiveHandler {

    /**
     * @param player      to progress
     * @param templateId  of the resource that was collected
     * @param profileData their quest data wrapper
     * @param quest       that is being progressed
     * @param objective   that triggered progress
     */
    private void incrementGatherObjective(Player player, String templateId, QuestProfileData profileData, Quest quest, QuestObjective objective) {
        QuestObjectiveGather gatherObjective = (QuestObjectiveGather) objective;
        for (String resourceId : gatherObjective.getTemplateIds()) {
            if (!templateId.equalsIgnoreCase(resourceId)) continue;
            gatherObjective.setResourcesGathered(gatherObjective.getResourcesGathered() + 1); // Add to the total resources gathered
            String displayName = RunicItemsAPI.generateItemFromTemplate(templateId).getDisplayableItem().getDisplayName();
            player.sendMessage(ChatColor.translateAlternateColorCodes
                    ('&', QuestsUtil.PREFIX + " Gather &f" + displayName + " &6» &7[&a" + gatherObjective.getResourcesGathered() + "&7/" + gatherObjective.getGatherAmount() + "]"));
            if (gatherObjective.getResourcesGathered().equals(gatherObjective.getGatherAmount())) { // # of req resources has been reached
                if (!questItemRequirementMet(player, objective)) continue;
                progressQuest(player, profileData, quest, objective);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onGather(GatheringEvent event) {
        if (event.isCancelled()) return;
        String templateId = event.getTemplateIdOfResource();
        runResourceGathered(event.getPlayer(), templateId);
    }

    /**
     * Each time a resource is gathered, checks if the given player has an objective to kill that mob
     *
     * @param player     who gathered the resource
     * @param templateId of the resource that was collected
     */
    private void runResourceGathered(Player player, String templateId) {
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
        QuestProfileData profileData = RunicQuests.getAPI().getQuestProfile(player.getUniqueId());
        for (Quest quest : profileData.getQuestsMap().get(slot)) {
            if (!isQuestActive(quest)) continue;
            for (QuestObjective objective : quest.getObjectives()) {
                if (!isValidObjective(quest, objective, QuestObjectiveType.GATHER)) continue;
                incrementGatherObjective(player, templateId, profileData, quest, objective);
            }
        }
    }

}