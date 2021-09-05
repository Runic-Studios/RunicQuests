package com.runicrealms.runicquests.listeners;

import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.event.EventInventory;
import org.bukkit.ChatColor;
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
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class JournalListener implements Listener {

    private static final ItemStack QUEST_JOURNAL = RunicItemsAPI.generateItemFromTemplate("quest-journal").generateItem();

    public static ItemStack getQuestJournal() {
        return QUEST_JOURNAL;
    }

    /**
     * Give new players the quest journal
     */
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
        if (e.isCancelled()) {
            return;
        }
        Player pl = (Player) e.getWhoClicked();
        int itemSlot = e.getSlot();
        if (itemSlot != 7) return;

        // don't trigger if there's no item in the slot to avoid null issues
        if (pl.getInventory().getItem(7) == null) return;
        ItemStack rune = pl.getInventory().getItem(7);

        ItemMeta meta = Objects.requireNonNull(rune).getItemMeta();
        if (meta == null) return;

        // only activate in survival mode to save builders the headache
        if (pl.getGameMode() != GameMode.SURVIVAL) return;

        // only listen for a player inventory
        if (e.getClickedInventory() == null) return;

        e.setCancelled(true);

        if (!(e.getClickedInventory().getType().equals(InventoryType.PLAYER))) return;

        EventInventory.openQuestGui(pl, 1);
    }

    @EventHandler
    public void onQuestJournalUse(PlayerInteractEvent e) {

        Player pl = e.getPlayer();

        if (pl.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (pl.getGameMode() == GameMode.CREATIVE) return;

        int slot = pl.getInventory().getHeldItemSlot();
        if (slot != 7) return;

        // annoying 1.9 feature which makes the event run twice, once for each hand
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;


        EventInventory.openQuestGui(pl, 1);
        e.setCancelled(true);
    }

    // cancel rune swapping
    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent e) {

        Player pl = e.getPlayer();
        int slot = pl.getInventory().getHeldItemSlot();

        if (slot == 7) {
            e.setCancelled(true);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.GRAY + "You cannot perform this action in this slot.");
        }
    }
}
