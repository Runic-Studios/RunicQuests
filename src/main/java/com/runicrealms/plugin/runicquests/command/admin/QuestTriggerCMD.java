package com.runicrealms.plugin.runicquests.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.runicquests.RunicQuests;
import com.runicrealms.plugin.runicquests.quests.trigger.TriggerObjectiveHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("questtrigger|qt")
@CommandPermission("runic.op")
public class QuestTriggerCMD extends BaseCommand {

    // questtrigger [player|party] [<party-member-name|player-name] [<trigger-id>]

    @CatchUnknown
    @Default
    @CommandCompletion("@playerOrParty @players @trigger-id")
    @Conditions("is-console-or-op")
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length != 3 && args.length != 4) {
            sender.sendMessage(ChatColor.RED + "Bad syntax! /questtrigger player|party <party-member-name|player-name> <trigger-id> <objective-name>");
            return;
        }

        Player player = Bukkit.getPlayerExact(args[1]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "That player doesn't exist!");
            return;
        }

        String triggerId = args[2];
        if (TriggerObjectiveHandler.getTrigger(triggerId) == null) {
            sender.sendMessage(ChatColor.RED + "Error, trigger for ID " + triggerId + " not found");
            return;
        }

        String objectiveName = args.length == 4 ? args[3] : null;

        RunicQuests.getAPI().triggerQuest(args[0].equalsIgnoreCase("party"), player, triggerId, objectiveName);
    }
}
