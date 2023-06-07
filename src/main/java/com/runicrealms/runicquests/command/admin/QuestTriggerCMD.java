package com.runicrealms.runicquests.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.model.QuestProfileData;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveHandler;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTrigger;
import com.runicrealms.runicquests.quests.trigger.Trigger;
import com.runicrealms.runicquests.quests.trigger.TriggerObjectiveHandler;
import com.runicrealms.runicquests.quests.trigger.TriggerType;
import com.runicrealms.runicquests.util.QuestsUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@CommandAlias("questtrigger|qt")
@CommandPermission("runic.op")
@SuppressWarnings("unused")
public class QuestTriggerCMD extends BaseCommand implements QuestObjectiveHandler {

    /**
     * @param player  who caused the trigger
     * @param players a set of players to receive credit from the trigger
     */
    private void addPartyMembersToTriggerCredit(Player player, Set<Player> players) {
        Party party = RunicCore.getPartyAPI().getParty(player.getUniqueId());
        if (party != null) {
            players.addAll(party.getMembersWithLeader());
        } else {
            players.add(player);
        }
    }

    /**
     * @param player  who caused trigger
     * @param trigger that was caused
     */
    private void handleTrigger(Player player, Trigger trigger) {
        int characterSlot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
        QuestProfileData profileData = RunicQuests.getAPI().getQuestProfile(player.getUniqueId());
        for (Quest quest : profileData.getQuestsMap().get(characterSlot)) {
            if (!isQuestActive(quest)) continue;
            if (quest.getQuestID() != trigger.getQuestId())
                continue; // Find the quest associated with trigger
            QuestObjectiveTrigger triggerObjective = (QuestObjectiveTrigger) QuestObjective.getObjective(quest.getObjectives(), trigger.getObjectiveId());
            if (triggerObjective == null) continue;
            for (QuestObjective objective : quest.getObjectives()) {
                // Ensure our objective is current and valid
                if (!isValidObjective(quest, objective, QuestObjectiveType.TRIGGER)) continue;
                // Ensure that our current objective matches the trigger objective
                if (!objective.getObjectiveNumber().equals(triggerObjective.getObjectiveNumber()))
                    continue;
                incrementTriggerObjective(player, trigger, profileData, quest, triggerObjective);
            }
        }
    }

    private void incrementTriggerObjective(Player player, Trigger trigger, QuestProfileData profileData, Quest quest, QuestObjectiveTrigger triggerObjective) {
        Set<String> triggersEarned = triggerObjective.getTriggersEarned();
        // How many triggers for this objective did they have before this one?
        int previousEarnedTriggerCount = triggersEarned.size();
        triggersEarned.add(trigger.getTriggerId());
        // If the player has achieved all triggers
        if (triggerObjective.getTriggerType() == TriggerType.ANY || triggersEarned.size() >= triggerObjective.getTriggerIds().size()) {
            // Handle trigger SYNC
            Bukkit.getScheduler().runTask(RunicQuests.getInstance(), () -> progressQuest(player, profileData, quest, triggerObjective));
        } else if (triggersEarned.size() > previousEarnedTriggerCount) { // Player discovered a unique trigger (set doesn't allow duplicates)
            player.sendMessage(ChatColor.translateAlternateColorCodes
                    ('&', QuestsUtil.PREFIX + " Hidden Trigger " + "&6» &7[&a" + triggerObjective.getTriggersEarned().size() + "&7/" + triggerObjective.getTriggerIds().size() + "]"));
        }
    }

    // questtrigger [player|party] [<party-member-name|player-name] [<trigger-id>]

    @CatchUnknown
    @Default
    @CommandCompletion("@playerOrParty @players @trigger-id")
    @Conditions("is-console-or-op")
    public void onCommand(CommandSender sender, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicQuests.getInstance(), () -> {
            if (args.length != 3) {
                sender.sendMessage(ChatColor.RED + "Bad syntax! /questtrigger player|party <party-member-name|player-name> <trigger-id>");
                return;
            }
            Set<Player> players = new HashSet<>();
            Player player = Bukkit.getPlayerExact(args[1]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "That player doesn't exist!");
                return;
            }
            players.add(player);
            if (args[0].equalsIgnoreCase("party")) {
                addPartyMembersToTriggerCredit(player, players);
            }
            String triggerId = args[2];
            Trigger trigger = TriggerObjectiveHandler.getTrigger(triggerId);
            if (trigger == null) {
                sender.sendMessage(ChatColor.RED + "Error, trigger for ID " + triggerId + " not found");
                return;
            }
            for (Player playerToReceiveCredit : players) {
                handleTrigger(playerToReceiveCredit, trigger);
            }
        });
    }
}
