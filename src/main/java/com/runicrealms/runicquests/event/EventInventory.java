package com.runicrealms.runicquests.event;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.util.RunicCoreHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EventInventory implements Listener {

    private static Map<UUID, Integer> playersInQuestGui = new HashMap<UUID, Integer>();

    public static void openQuestGui(Player player) {
        Map<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
        QuestProfile profile = Plugin.getQuestProfile(player.getUniqueId().toString());
        List<Quest> startedQuests = new ArrayList<Quest>();
        List<Quest> unstartedQuests = new ArrayList<Quest>();
        List<Quest> completedQuests = new ArrayList<Quest>();
        for (Quest quest : profile.getQuests()) {
            if (quest.getQuestState().hasStarted() && quest.getQuestState().isCompleted() == false) {
                startedQuests.add(quest);
            }
            if (quest.getQuestState().hasStarted() == false && quest.getQuestState().isCompleted() == false) {
                unstartedQuests.add(quest);
            }
            if (quest.getQuestState().isCompleted()) {
                completedQuests.add(quest);
            }
        }
        Collections.sort(startedQuests, new Comparator<Quest>(){
            public int compare(Quest a, Quest b){
                if (a.getRequirements().getClassLvReq() > b.getRequirements().getClassLvReq()) {
                    return 1;
                } else if (a.getRequirements().getClassLvReq() < b.getRequirements().getClassLvReq()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        Collections.sort(unstartedQuests, new Comparator<Quest>() {
            public int compare(Quest a, Quest b){
                if (a.getRequirements().getClassLvReq() > b.getRequirements().getClassLvReq()) {
                    return 1;
                } else if (a.getRequirements().getClassLvReq() < b.getRequirements().getClassLvReq()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        Collections.sort(completedQuests, new Comparator<Quest>() {
            public int compare(Quest a, Quest b){
                if (a.getRequirements().getClassLvReq() > b.getRequirements().getClassLvReq()) {
                    return 1;
                } else if (a.getRequirements().getClassLvReq() < b.getRequirements().getClassLvReq()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        int itemsAdded = 0;
        for (int i = 0; i < startedQuests.size(); i++) {
            if (itemsAdded < 27) {
                items.put(items.size(), startedQuests.get(i).generateQuestIcon(player));
                itemsAdded++;
            }
        }
        for (int i = 0; i < unstartedQuests.size(); i++) {
            if (itemsAdded < 27) {
                items.put(items.size(), unstartedQuests.get(i).generateQuestIcon(player));
                itemsAdded++;
            }
        }
        for (int i = 0; i < completedQuests.size(); i++) {
            if (itemsAdded < 27) {
                items.put(items.size(), completedQuests.get(i).generateQuestIcon(player));
                itemsAdded++;
            }
        }
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "Quests"));
        for (Map.Entry<Integer, ItemStack> item : items.entrySet()) {
            inventory.setItem(item.getKey(), item.getValue());
        }
        player.closeInventory();
        player.openInventory(inventory);
        playersInQuestGui.put(player.getUniqueId(), 1);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (playersInQuestGui.containsKey(event.getPlayer().getUniqueId())) {
            playersInQuestGui.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerJoinEvent event) {
        if (playersInQuestGui.containsKey(event.getPlayer().getUniqueId())) {
            playersInQuestGui.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (playersInQuestGui.containsKey(((Player) event.getWhoClicked()).getUniqueId())) {
            event.setCancelled(true);
        }
    }

}
