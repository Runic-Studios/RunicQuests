package com.runicrealms.runicquests.command;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.config.PlayerDataLoader;
import com.runicrealms.runicquests.event.EventPlayerJoinQuit;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.FirstNpcState;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.objective.QuestObjective;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CompleteQuestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player == false) {
            sender.sendMessage(ChatColor.RED + "Only players can use this!");
            return true;
        }
        Player player = (Player) sender;
        if (player.isOp()) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Use /cq <quest>");
            } else {
                QuestProfile profile = Plugin.getQuestProfile(player.getUniqueId().toString());
                boolean completed = false;
                for (Quest quest : profile.getQuests()) {
                    if (quest.getQuestName().equalsIgnoreCase(combineArgs(args, 0))) {
                        quest.getQuestState().setCompleted(true);
                        quest.getQuestState().setStarted(true);
                        quest.getFirstNPC().setState(FirstNpcState.ACCEPTED);
                        for (QuestObjective objective : quest.getObjectives()) {
                            objective.setCompleted(true);
                        }
                        completed = true;
                        break;
                    }
                }
                if (completed) {
                    Integer slot = new Integer(profile.getCharacterSlot());
                    PlayerDataLoader.getConfigFromCache(player.getUniqueId()).saveToConfig(profile.getQuests(), profile.getCharacterSlot());
                    EventPlayerJoinQuit.runQuitEvent(player);
                    EventPlayerJoinQuit.runJoinEvent(player, slot);
                    player.sendMessage(ChatColor.GREEN + "Completed quest \"" + combineArgs(args, 0) + "\"!");
                } else {
                    player.sendMessage(ChatColor.RED + "Quest \"" + combineArgs(args, 0) + "\" does not exist.");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Only operators can use this!");
        }
        return true;
    }

    private static String combineArgs(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            builder.append(args[i]);
            if (i != args.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

}
