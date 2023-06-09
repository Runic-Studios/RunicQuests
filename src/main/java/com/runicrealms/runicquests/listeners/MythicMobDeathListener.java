package com.runicrealms.runicquests.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
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
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MythicMobDeathListener implements Listener, QuestObjectiveHandler {
    private static final int KILL_COUNT_DIST_SQUARED = 1024; // Represents max range of party members (32 blocks) to receive credit
    private final Map<UUID, Set<UUID>> questBossFighters; // A single quest boss is mapped to many players

    public MythicMobDeathListener() {
        questBossFighters = new ConcurrentHashMap<>();
    }

    /**
     * @param entity that will be tracked
     * @return true if it is the name of a dungeon boss
     */
    public static boolean isQuestBoss(Entity entity) {
        if (!MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId()).isPresent()) return false;
        ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId()).get();
        return am.hasFaction() && am.getFaction().equalsIgnoreCase("boss");
    }

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

    @EventHandler(priority = EventPriority.HIGHEST) // runs late
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        addBossParticipant(event.getPlayer(), event.getVictim());
    }

    @EventHandler(priority = EventPriority.HIGHEST) // runs late
    public void onSpellDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        addBossParticipant(event.getPlayer(), event.getVictim());
    }

    /**
     * Keeps track of damage during boss fight to determine who gets loot priority.
     *
     * @param player who damaged boss
     * @param entity the boss
     */
    private void addBossParticipant(Player player, Entity entity) {
//        if (!isQuestBoss(entity)) return;
        UUID playerId = player.getUniqueId();
        UUID bossId = entity.getUniqueId();
        questBossFighters.computeIfAbsent(entity.getUniqueId(), k -> new HashSet<>());
        questBossFighters.get(bossId).add(playerId);
    }

    @EventHandler
    public void onKill(MythicMobDeathEvent event) {
        if (!(event.getKiller() instanceof Player)) return;
        UUID mobUUID = event.getEntity().getUniqueId();

        Set<UUID> fighters = new HashSet<>(questBossFighters.get(mobUUID));
        Set<UUID> processed = new HashSet<>();

        // Iterate through the list of fighters
        for (UUID uuid : fighters) {
            if (processed.contains(uuid)) continue; // Skip if already processed

            Party party = RunicCore.getPartyAPI().getParty(uuid); // Of participant
            if (party != null) {
                // If player is in a party, loop through each party member
                for (Player player : party.getMembersWithLeader()) {
                    if (player.getLocation().getWorld() != event.getKiller().getLocation().getWorld())
                        continue;
                    if (player.getLocation().distanceSquared(event.getKiller().getLocation()) > KILL_COUNT_DIST_SQUARED)
                        continue;

                    // Only give credit if this player is not already processed
                    if (!processed.contains(player.getUniqueId())) {
                        runMythicMobsKill(player, event.getMobType());
                        processed.add(player.getUniqueId()); // Mark as processed
                    }
                }
            } else {
                // This is the original killer or a solo fighter, not part of any party
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    runMythicMobsKill(player, event.getMobType());
                    processed.add(uuid); // Mark as processed
                }
            }
        }

        // Clear tracking map
        questBossFighters.get(mobUUID).clear();
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