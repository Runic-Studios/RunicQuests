package com.runicrealms.plugin.runicquests.listeners;

import com.runicrealms.plugin.professions.event.RunicCraftEvent;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicquests.RunicQuests;
import com.runicrealms.plugin.runicquests.model.QuestProfileData;
import com.runicrealms.plugin.runicquests.quests.Quest;
import com.runicrealms.plugin.runicquests.quests.QuestObjectiveType;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjective;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveCraft;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveHandler;
import com.runicrealms.plugin.runicquests.util.QuestsUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * A class that contains logic for the crafting quest objective which is basically copy pasted from the gathering one
 *
 * @author BoBoBalloon
 * @since 11/23/23
 */
public class CraftListener implements Listener, QuestObjectiveHandler {

    /**
     * @param player      to progress
     * @param templateId  of the resource that was collected
     * @param profileData their quest data wrapper
     * @param quest       that is being progressed
     * @param objective   that triggered progress
     */
    private void incrementCraftObjective(@NotNull Player player, @NotNull String templateId, @NotNull QuestProfileData profileData, @NotNull Quest quest, @NotNull QuestObjective objective, int amount) {
        QuestObjectiveCraft craftObjective = (QuestObjectiveCraft) objective;
        for (String resourceId : craftObjective.getTemplateIds()) {
            if (!templateId.equalsIgnoreCase(resourceId)) continue;
            craftObjective.setCrafted(craftObjective.getCrafted() + amount); // Add to the total resources gathered
            String displayName = RunicItemsAPI.generateItemFromTemplate(templateId).getDisplayableItem().getDisplayName();
            player.sendMessage(ChatColor.translateAlternateColorCodes
                    ('&', QuestsUtil.PREFIX + " Craft &f" + displayName + " &6» &7[&a" + craftObjective.getCrafted() + "&7/" + craftObjective.getRequiredToComplete() + "]"));
            if (craftObjective.getCrafted() >= craftObjective.getRequiredToComplete()) { // # of req resources has been reached
                if (!questItemRequirementMet(player, objective)) continue;
                progressQuest(player, profileData, quest, objective);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRunicCraft(RunicCraftEvent event) {
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(event.getPlayer().getUniqueId());
        QuestProfileData profileData = RunicQuests.getAPI().getQuestProfile(event.getPlayer().getUniqueId());

        for (Quest quest : profileData.getQuestsMap().get(slot)) {
            if (!isQuestActive(quest)) continue;
            for (QuestObjective objective : quest.getObjectives()) {
                if (!isValidObjective(quest, objective, QuestObjectiveType.CRAFT)) continue;
                incrementCraftObjective(event.getPlayer(), event.getProduct().getTemplateId(), profileData, quest, objective, event.getAmount());
            }
        }
    }
}