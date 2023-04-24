package com.runicrealms.runicquests.ui;

import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

public class QuestMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof QuestMenu)) return;
        Player player = (Player) event.getWhoClicked();
        QuestMenu questMenu = (QuestMenu) event.getInventory().getHolder();
        if (questMenu == null) return;
        event.setCancelled(true);
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR)
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().equals(QuestMenu.TRACK_COMPASS_ITEM)) return;
        if (event.getCurrentItem().equals(QuestMenu.ACTIVE_QUEST_ITEM)) return;
        if (event.getCurrentItem().getType() == Material.PAPER) return; // info items
        if (event.getCurrentItem().equals(GUIUtil.CLOSE_BUTTON)) {
            player.closeInventory();
        } else if (event.getCurrentItem().equals(GUIUtil.BACK_BUTTON)) {
            questMenu.openFirstPage();
        } else if (event.getCurrentItem().equals(GUIUtil.FORWARD_BUTTON)) {
            questMenu.openNextPage();
        } else if (event.getCurrentItem().equals(QuestMenu.TOGGLE_SHOW_REPEATABLE_QUESTS_ITEM)) {
            questMenu.setShowRepeatableQuests(!questMenu.getShowRepeatableQuests());
            questMenu.openFirstPage();
        } else if (event.getCurrentItem().equals(QuestMenu.DISABLE_COMPASS_ITEM)) {
            CompassManager.revertCompass(player);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Compass tracking is now disabled.");
        } else { // any quest ItemStack
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            attemptToTrackQuest((Player) event.getWhoClicked(), meta);
        }
    }

    /**
     * For the given ItemStack, attempts to strip location coordinates for the quest compass
     *
     * @param player who clicked the inventory
     * @param meta   of the ItemStack
     */
    private void attemptToTrackQuest(Player player, ItemMeta meta) {
        if (meta == null) return;
        if (meta.getLore() == null) return;
        if (!player.getWorld().getName().equalsIgnoreCase("alterra")) {
            player.sendMessage(ChatColor.RED + "Compass tracking is disabled in the dungeon world!");
            player.closeInventory();
            return;
        }
        for (String lore : meta.getLore()) {
            String stripped = ChatColor.stripColor(lore);
            Optional<Location> opt = CompassManager.parseLocation(stripped.replaceAll("location: ", ""), player.getWorld());
            if (opt.isPresent()) {
                Location location = opt.get();
                CompassManager.CompassLocation comp = new CompassManager.CompassLocation(location, ChatColor.stripColor(meta.getDisplayName()), stripped);
                CompassManager.setCompass(player, comp);
                comp.send(player);
                player.closeInventory();
                return;
            }
        }
        player.sendMessage(ChatColor.RED + "No coordinates found for this objective!");
    }
}
