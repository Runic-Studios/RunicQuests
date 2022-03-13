package com.runicrealms.runicquests.ui;

import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicquests.Plugin;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

/**
 * Give new players the quest journal, controls opening of inventory
 */
public class JournalListener implements Listener {

    private static final ItemStack QUEST_JOURNAL = RunicItemsAPI.generateItemFromTemplate("quest-journal").generateItem();

    public static ItemStack getQuestJournal() {
        return QUEST_JOURNAL;
    }

    @EventHandler
    public void onCharacterLoad(CharacterLoadEvent e) {
        Player pl = e.getPlayer();
        if (pl.getGameMode() != GameMode.SURVIVAL) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                pl.getInventory().setItem(7, getQuestJournal());
                pl.updateInventory();
            }
        }.runTaskLater(Plugin.getInstance(), 2L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.isCancelled()) return;
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() != InventoryType.PLAYER) return;
        Player player = (Player) e.getWhoClicked();
        int itemSlot = e.getSlot();
        if (itemSlot != 7) return;

        // don't trigger if there's no item in the slot to avoid null issues
        if (player.getInventory().getItem(7) == null) return;
        ItemStack rune = player.getInventory().getItem(7);

        ItemMeta meta = Objects.requireNonNull(rune).getItemMeta();
        if (meta == null) return;

        // only activate in survival mode to save builders the headache
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        // only listen for a player inventory
        if (e.getClickedInventory() == null) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onQuestJournalUse(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        int slot = player.getInventory().getHeldItemSlot();
        if (slot != 7) return;
        // annoying 1.9 feature which makes the event run twice, once for each hand
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        player.openInventory(new QuestMenu(player).getInventory());
        e.setCancelled(true);
    }
}
