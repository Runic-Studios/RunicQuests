package com.runicrealms.runicquests.command;

import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.event.EventPlayerJoinQuit;
import com.runicrealms.runicquests.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ResetQuestsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player == false) {
            sender.sendMessage(ChatColor.RED + "Only players can use this!");
            return true;
        }
        Player player = (Player) sender;
        if (player.isOp()) {
            List<Quest> quests = new ArrayList<Quest>();
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Use /rq <player> <quest> or /rq <player>");
            } else if (args.length == 1) {
                Player otherPlayer = Bukkit.getPlayer(args[0]);
                if (otherPlayer != null) {
                    QuestProfile profile = PlayerDataLoader.getPlayerQuestData(otherPlayer.getUniqueId());
                    for (Quest quest : profile.getQuests()) {
                        quests.add(quest.clone());
                    }
                    Integer slot = new Integer(profile.getSlot());
                    profile.save(quests);
                    EventPlayerJoinQuit.refreshPlayerData(profile, otherPlayer);
                    player.sendMessage(ChatColor.GREEN + "Reset their quest data!");
                } else {
                    player.sendMessage(ChatColor.RED + "Player \"" + args[0] + "\" is not online.");
                }
            } else {
                Player otherPlayer = Bukkit.getPlayer(args[0]);
                if (otherPlayer != null) {
                    QuestProfile profile = PlayerDataLoader.getPlayerQuestData(otherPlayer.getUniqueId());
                    boolean reset = false;
                    for (Quest quest : profile.getQuests()) {
                        if (quest.getQuestName().equalsIgnoreCase(combineArgs(args, 1))) {
                            quests.add(quest.clone());
                            reset = true;
                            break;
                        } else {
                            quests.add(quest);
                        }
                    }
                    if (reset) {
                        Integer slot = new Integer(profile.getSlot());
                        profile.save(quests);
                        EventPlayerJoinQuit.refreshPlayerData(profile, otherPlayer);
                        player.sendMessage(ChatColor.GREEN + "Reset their quest data for quest \"" + combineArgs(args, 1) + "\"!");
                    } else {
                        player.sendMessage(ChatColor.RED + "Quest \"" + combineArgs(args, 1) + "\" does not exist.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Player \"" + args[0] + "\" is not online.");
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
