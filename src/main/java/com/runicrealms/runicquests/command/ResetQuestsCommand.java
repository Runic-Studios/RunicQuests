package com.runicrealms.runicquests.command;

import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.config.PlayerDataLoader;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
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
            QuestProfile profile = Plugin.getQuestProfile(player.getUniqueId().toString());
            if (args.length == 0) {
                for (Quest quest : profile.getQuests()) {
                    quests.add(quest.clone());
                }

                player.sendMessage(ChatColor.GREEN + "Reset your quest data!");
            } else {
                for (Quest quest : profile.getQuests()) {
                    if (quest.getQuestName().equalsIgnoreCase(combineArgs(args, 0))) {
                        quests.add(quest.clone());
                    } else {
                        quests.add(quest);
                    }
                }
                player.sendMessage(ChatColor.GREEN + "Reset your quest data for quest \"" + combineArgs(args, 0) + "\"!");
            }
            PlayerDataLoader.getConfigFromCache(player.getUniqueId()).saveToConfig(quests, profile.getCharacterSlot());
            Plugin.getQuestProfiles().remove(Plugin.getQuestProfile(player.getUniqueId().toString()));
            Plugin.getQuestProfiles().add(new QuestProfile(player.getUniqueId(), RunicCharactersApi.getCurrentCharacterSlot(player.getUniqueId())));
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
