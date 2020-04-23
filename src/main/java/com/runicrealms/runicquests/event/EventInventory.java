package com.runicrealms.runicquests.event;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.util.RunicCoreHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EventInventory implements Listener {

    private static Map<UUID, Integer> playersInQuestGui = new HashMap<UUID, Integer>();

    public static void openQuestGui(Player player, Integer page) {
        Map<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
        List<Quest> quests = getSortedQuests(player);
        for (int i = (page - 1) * 26; i < page * 26; i++) {
            if (i + 1 <= quests.size()) {
                if (quests.get(i).getRequirements().hasClassTypeRequirement()) {
                    if (!RunicCoreHook.isRequiredClass(quests.get(i).getRequirements().getClassTypeRequirement(), player)) continue;
                }
                if (quests.get(i).getRequirements().hasClassTypeRequirement()) {
                    if (!RunicCoreHook.hasProfession(player, quests.get(i).getRequirements().getCraftingProfessionType())) continue;
                }
                items.put(i - (page - 1) * 26, quests.get(i).generateQuestIcon(player));
            }
        }
        if (quests.size() - page * 26 > 0) {
            ItemStack arrow = new ItemStack(Material.ARROW);
            ItemMeta meta = arrow.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Next Page");
            arrow.setItemMeta(meta);
            items.put(26, arrow);
        }
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "Quests"));
        for (Map.Entry<Integer, ItemStack> item : items.entrySet()) {
            inventory.setItem(item.getKey(), item.getValue());
        }
        player.closeInventory();
        player.openInventory(inventory);
        playersInQuestGui.put(player.getUniqueId(), page);
    }

    private static List<Quest> getSortedQuests(Player player) {
        QuestProfile profile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId());
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
            public int compare(Quest a, Quest b) {
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
            public int compare(Quest a, Quest b) {
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
            public int compare(Quest a, Quest b) {
                if (a.getRequirements().getClassLvReq() > b.getRequirements().getClassLvReq()) {
                    return 1;
                } else if (a.getRequirements().getClassLvReq() < b.getRequirements().getClassLvReq()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        List<Quest> sorted = new ArrayList<Quest>();
        for (Quest quest : startedQuests) {
            sorted.add(quest);
        }
        for (Quest quest : unstartedQuests) {
            sorted.add(quest);
        }
        for (Quest quest : completedQuests) {
            sorted.add(quest);
        }
        return sorted;
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
            if (event.getCurrentItem() != null) {
                if (event.getCurrentItem().getType() == Material.ARROW) {
                    openQuestGui((Player) event.getWhoClicked(), playersInQuestGui.get(((Player) event.getWhoClicked()).getUniqueId()) + 1);
                }
            }
        }
    }

}
