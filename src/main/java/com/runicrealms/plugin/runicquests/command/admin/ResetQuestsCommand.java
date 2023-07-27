//package com.runicrealms.runicquests.command.admin;
//
//import com.runicrealms.plugin.RunicCore;
//import com.runicrealms.runicquests.RunicQuests;
//import com.runicrealms.runicquests.model.QuestProfileData;
//import com.runicrealms.runicquests.quests.Quest;
//import com.runicrealms.runicquests.util.QuestsUtil;
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import redis.clients.jedis.Jedis;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ResetQuestsCommand implements CommandExecutor {
//
//    private static String combineArgs(String[] args, int start) {
//        StringBuilder builder = new StringBuilder();
//        for (int i = start; i < args.length; i++) {
//            builder.append(args[i]);
//            if (i != args.length - 1) {
//                builder.append(" ");
//            }
//        }
//        return builder.toString();
//    }
//
//    @Override
//    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//        if (!(sender instanceof Player)) {
//            sender.sendMessage(ChatColor.RED + "Only players can use this!");
//            return true;
//        }
//        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
//            Player player = (Player) sender;
//            if (player.isOp()) {
//                List<Quest> quests = new ArrayList<>();
//                if (args.length == 0) {
//                    player.sendMessage(ChatColor.RED + "Use /rq <player> <quest> or /rq <player>");
//                } else if (args.length == 1) {
//                    Player otherPlayer = Bukkit.getPlayer(args[0]);
//                    if (otherPlayer != null) {
//                        int slot = RunicCore.getCharacterAPI().getCharacterSlot(otherPlayer.getUniqueId());
//                        QuestProfileData profile = RunicQuests.getQuestsAPI().getQuestProfile(otherPlayer.getUniqueId(), slot);
//                        for (Quest quest : profile.getQuests()) {
//                            quests.add(quest.clone());
//                        }
//                        profile.writeToJedis(jedis, profile.getSlot());
//                        QuestsUtil.refreshPlayerData(otherPlayer, slot);
//                        player.sendMessage(ChatColor.GREEN + "Reset their quest data!");
//                    } else {
//                        player.sendMessage(ChatColor.RED + "Player \"" + args[0] + "\" is not online.");
//                    }
//                } else {
//                    Player otherPlayer = Bukkit.getPlayer(args[0]);
//                    if (otherPlayer != null) {
//                        int slot = RunicCore.getCharacterAPI().getCharacterSlot(otherPlayer.getUniqueId());
//                        QuestProfileData profile = RunicQuests.getQuestsAPI().getQuestProfile(otherPlayer.getUniqueId(), slot);
//                        boolean reset = false;
//                        for (Quest quest : profile.getQuests()) {
//                            if (quest.getQuestName().equalsIgnoreCase(combineArgs(args, 1))) {
//                                quests.add(quest.clone());
//                                reset = true;
//                                break;
//                            } else {
//                                quests.add(quest);
//                            }
//                        }
//                        if (reset) {
//                            profile.writeToJedis(jedis, profile.getSlot());
//                            QuestsUtil.refreshPlayerData(otherPlayer, slot);
//                            player.sendMessage(ChatColor.GREEN + "Reset their quest data for quest \"" + combineArgs(args, 1) + "\"!");
//                        } else {
//                            player.sendMessage(ChatColor.RED + "Quest \"" + combineArgs(args, 1) + "\" does not exist.");
//                        }
//                    } else {
//                        player.sendMessage(ChatColor.RED + "Player \"" + args[0] + "\" is not online.");
//                    }
//                }
//            } else {
//                player.sendMessage(ChatColor.RED + "Only operators can use this!");
//            }
//            return true;
//        }
//    }
//
//
//}
