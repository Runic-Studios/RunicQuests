package com.runicrealms.runicquests.command;

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
            for (Quest quest : profile.getQuests()) {
                quests.add(quest.clone());
            }
            PlayerDataLoader.getConfigFromCache(player.getUniqueId()).saveToConfig(quests, profile.getCharacterSlot());
            player.sendMessage(ChatColor.GREEN + "Reset your quest data!");
        } else {
            player.sendMessage(ChatColor.RED + "Only operators can use this!");
        }
        return true;
    }

}
