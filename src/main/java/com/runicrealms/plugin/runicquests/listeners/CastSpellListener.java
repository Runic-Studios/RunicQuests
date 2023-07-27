package com.runicrealms.plugin.runicquests.listeners;

import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.runicquests.model.QuestProfileData;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjective;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveCast;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveHandler;
import com.runicrealms.plugin.runicquests.util.QuestsUtil;
import com.runicrealms.plugin.runicquests.RunicQuests;
import com.runicrealms.plugin.runicquests.quests.Quest;
import com.runicrealms.plugin.runicquests.quests.QuestObjectiveType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CastSpellListener implements Listener, QuestObjectiveHandler {

    /**
     * @param player      to progress
     * @param spellName   that was cast
     * @param profileData their quest data wrapper
     * @param quest       that is being progressed
     * @param objective   that triggered progress
     */
    private void incrementCastObjective(Player player, String spellName, QuestProfileData profileData, Quest quest, QuestObjective objective) {
        QuestObjectiveCast castObjective = (QuestObjectiveCast) objective;
        for (String objectiveSpell : castObjective.getSpellNames()) {
            if (!(spellName.equalsIgnoreCase(objectiveSpell) || objectiveSpell.equalsIgnoreCase("*")))
                continue;
            castObjective.setSpellsCasted(castObjective.getSpellsCasted() + 1); // Add to the total spells cast
            player.sendMessage(ChatColor.translateAlternateColorCodes
                    ('&', QuestsUtil.PREFIX + " Cast Spell &f" + spellName + " &6» &7[&a" + castObjective.getSpellsCasted() + "&7/" + castObjective.getSpellAmount() + "]"));
            if (castObjective.getSpellsCasted().equals(castObjective.getSpellAmount())) { // # of req spells has been reached
                if (!questItemRequirementMet(player, objective)) continue;
                progressQuest(player, profileData, quest, objective);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCast(SpellCastEvent event) {
        String spellName = event.getSpell().getName();

        //account for combat and potion "spells"
        if (spellName.equalsIgnoreCase("Combat") || spellName.equalsIgnoreCase("Potion")) {
            return;
        }

        runSpellCast(event.getCaster(), spellName);
    }

    /**
     * Each time a Mythic Mob is killed, checks if the given player has an objective to kill that mob
     *
     * @param player    who cast the spell
     * @param spellName that was cast
     */
    private void runSpellCast(Player player, String spellName) {
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
        QuestProfileData profileData = RunicQuests.getAPI().getQuestProfile(player.getUniqueId());
        for (Quest quest : profileData.getQuestsMap().get(slot)) {
            if (!isQuestActive(quest)) continue;
            for (QuestObjective objective : quest.getObjectives()) {
                if (!isValidObjective(quest, objective, QuestObjectiveType.CAST)) continue;
                incrementCastObjective(player, spellName, profileData, quest, objective);
            }
        }
    }

}