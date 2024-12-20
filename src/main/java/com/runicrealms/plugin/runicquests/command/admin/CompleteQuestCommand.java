//package com.runicrealms.runicquests.command.admin;
//
//import com.runicrealms.plugin.RunicCore;
//import com.runicrealms.runicquests.RunicQuests;
//import com.runicrealms.runicquests.api.QuestCompleteEvent;
//import com.runicrealms.runicquests.model.QuestProfileData;
//import com.runicrealms.runicquests.quests.FirstNpcState;
//import com.runicrealms.runicquests.quests.Quest;
//import com.runicrealms.runicquests.quests.objective.QuestObjective;
//import com.runicrealms.runicquests.util.QuestsUtil;
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import redis.clients.jedis.Jedis;
//
//public class CompleteQuestCommand implements CommandExecutor {
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
//            sender.sendMessage(ChatColor.RED + "This command cannot be run from console!");
//            return true;
//        }
//        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
//            Player player = (Player) sender;
//            if (player.isOp()) {
//                if (args.length == 0) {
//                    player.sendMessage(ChatColor.RED + "Use /cq <quest>");
//                } else {
//                    int slot = RunicCore.getCharacterAPI().getCharacterSlot(player.getUniqueId());
//                    QuestProfileData profile = RunicQuests.getQuestsAPI().getQuestProfile(player.getUniqueId(), slot);
//                    boolean completed = false;
//                    for (Quest quest : profile.getQuests()) {
//                        if (quest.getQuestName().equalsIgnoreCase(combineArgs(args, 0))) {
//                            quest.getQuestState().setCompleted(true);
//                            quest.getQuestState().setStarted(true);
//                            quest.getFirstNPC().setState(FirstNpcState.ACCEPTED);
//                            for (QuestObjective objective : quest.getObjectives()) {
//                                objective.setCompleted(true);
//                            }
//                            completed = true;
//                            Bukkit.getServer().getPluginManager().callEvent(new QuestCompleteEvent(quest, profile)); // Call the quest event
//                            break;
//                        }
//                    }
//                    if (completed) {
//                        profile.writeToJedis(jedis, profile.getSlot());
//                        QuestsUtil.refreshPlayerData(player, slot);
//                        player.sendMessage(ChatColor.GREEN + "Completed quest \"" + combineArgs(args, 0) + "\"!");
//                    } else {
//                        player.sendMessage(ChatColor.RED + "Quest \"" + combineArgs(args, 0) + "\" does not exist.");
//                    }
//                }
//            } else {
//                player.sendMessage(ChatColor.RED + "Only operators can use this!");
//            }
//            return true;
//        }
//    }
//
//}
