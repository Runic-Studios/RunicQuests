package com.runicrealms.runicquests.command;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.runicquests.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class QuestTriggerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp()) {
            Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
                if (args.length == 3) {
                    Set<Player> players = new HashSet<Player>();
                    if (args[0].equalsIgnoreCase("player")) {
                        Player player = null;
                        if (args[1].equalsIgnoreCase("@p")) {
                            if (sender instanceof BlockCommandSender) {
                                BlockCommandSender blockSender = (BlockCommandSender) sender;
                                double current = -1;
                                for (Player target : Bukkit.getOnlinePlayers()) {
                                    double distanceSquared = target.getLocation().distanceSquared(blockSender.getBlock().getLocation());
                                    if (current == -1 || target.getLocation().distanceSquared(blockSender.getBlock().getLocation()) < current) {
                                        current = distanceSquared;
                                        player = target;
                                    }
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "This can only be run from a command block!");
                                return;
                            }
                        } else {
                            player = Bukkit.getPlayerExact(args[1]);
                        }
                        if (player != null) {
                            players.add(player);
                        } else {
                            sender.sendMessage(ChatColor.RED + "That player doesn't exist!");
                            return;
                        }
                    } else if (args[0].equalsIgnoreCase("party")) {
                        Player partyMember = null;
                        if (args[1].equalsIgnoreCase("@p")) {
                            if (sender instanceof BlockCommandSender) {
                                BlockCommandSender blockSender = (BlockCommandSender) sender;
                                double current = -1;
                                for (Player target : Bukkit.getOnlinePlayers()) {
                                    double distanceSquared = target.getLocation().distanceSquared(blockSender.getBlock().getLocation());
                                    if (target.getLocation().getWorld() == blockSender.getBlock().getWorld()) {
                                        if (current == -1 || target.getLocation().distanceSquared(blockSender.getBlock().getLocation()) < current) {
                                            current = distanceSquared;
                                            partyMember = target;
                                        }
                                    }
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "This can only be run from a command block!");
                                return;
                            }
                        } else {
                            partyMember = Bukkit.getPlayerExact(args[1]);
                        }
                        if (partyMember != null) {
                            Party party = RunicCore.getPartyManager().getPlayerParty(partyMember);
                            if (party != null) {
                                players.addAll(party.getMembersWithLeader());
                            } else {
                                players.add(partyMember);
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "That player doesn't exist!");
                            return;
                        }
                        // TODO trigger
                    } else {
                        sender.sendMessage(ChatColor.RED + "Bad syntax! /questtrigger player|party <party-member-name|player-name|@p> <trigger-id>");
                        return;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Bad syntax! /questtrigger player|party <party-member-name|player-name|@p> <trigger-id>");
                }
            });
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
        }
        return true;
    }

}
