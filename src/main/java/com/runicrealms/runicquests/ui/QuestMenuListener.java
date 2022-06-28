package com.runicrealms.runicquests.ui;

import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

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
        if (event.getCurrentItem().equals(GUIUtil.closeButton())) {
            player.closeInventory();
        } else if (event.getCurrentItem().equals(GUIUtil.backButton())) {
            questMenu.openFirstPage();
        } else if (event.getCurrentItem().equals(QuestMenu.FORWARD_ARROW)) {
            questMenu.openNextPage();
        } else if (event.getCurrentItem().equals(QuestMenu.toggleShowRepeatableQuestsItem)) {
            questMenu.setShowRepeatableQuests(!questMenu.getShowRepeatableQuests());
            questMenu.openFirstPage();
        } else if (event.getCurrentItem().equals(QuestMenu.disableCompassItem)) {
            CompassManager.revertCompass(player);
            event.getWhoClicked().sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Tracking is now disabled.");
        }
    }
}
