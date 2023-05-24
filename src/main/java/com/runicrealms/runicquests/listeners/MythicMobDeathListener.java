package com.runicrealms.runicquests.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.model.QuestProfileData;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveHandler;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveSlay;
import com.runicrealms.runicquests.util.QuestsUtil;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicMobDeathListener implements Listener, QuestObjectiveHandler {
    private static final int KILL_COUNT_DIST_SQUARED = 1024; // Represents max range of party members (32 blocks) to receive credit

    /**
     * @param player      to progress
     * @param mythicMob   that was killed
     * @param profileData their quest data wrapper
     * @param quest       that is being progressed
     * @param objective   that triggered progress
     */
    private void incrementSlayObjective(Player player, MythicMob mythicMob, QuestProfileData profileData, Quest quest, QuestObjective objective) {
        QuestObjectiveSlay slayObjective = (QuestObjectiveSlay) objective;
        for (String mob : slayObjective.getMobNames()) {
            if (!mythicMob.getInternalName().equalsIgnoreCase(mob)) continue;
            slayObjective.setMobsKilled(slayObjective.getMobsKilled() + 1); // Add to the slayed mobs
            player.sendMessage(ChatColor.translateAlternateColorCodes
                    ('&', QuestsUtil.PREFIX + " " + mythicMob.getDisplayName() + " &6» &7[&a" + slayObjective.getMobsKilled() + "&7/" + slayObjective.getMobAmount() + "]"));
            if (slayObjective.getMobsKilled().equals(slayObjective.getMobAmount())) { // # of req mobs has been reached
                if (!questItemRequirementMet(player, objective)) continue;
                progressQuest(player, profileData, quest, objective);
            }
        }
    }

    @EventHandler
    public void onKill(MythicMobDeathEvent event) {
        if (!(event.getKiller() instanceof Player)) return;
        Party party = RunicCore.getPartyAPI().getParty(event.getKiller().getUniqueId());
        if (party != null) {
            for (Player player : party.getMembersWithLeader()) {
                if (player.getLocation().getWorld() != event.getKiller().getLocation().getWorld())
                    continue;
                if (player.getLocation().distanceSquared(event.getKiller().getLocation()) > KILL_COUNT_DIST_SQUARED)
                    continue;
                runMythicMobsKill(player, event.getMobType());
            }
        } else {
            runMythicMobsKill((Player) event.getKiller(), event.getMobType());
        }
    }

    /**
     * Each time a Mythic Mob is killed, checks if the given player has an objective to kill that mob
     *
     * @param player    that killed the mob
     * @param mythicMob that was killed
     */
    private void runMythicMobsKill(Player player, MythicMob mythicMob) {
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
        QuestProfileData profileData = RunicQuests.getAPI().getQuestProfile(player.getUniqueId()); // Get player questing profile
        for (Quest quest : profileData.getQuestsMap().get(slot)) { // Loop through quest to find a matching objective to the mob killed
            if (!isQuestActive(quest)) continue;
            for (QuestObjective objective : quest.getObjectives()) { // Loop through objectives to find a match
                if (!isValidObjective(quest, objective, QuestObjectiveType.SLAY)) continue;
                incrementSlayObjective(player, mythicMob, profileData, quest, objective);
            }
        }
    }

}