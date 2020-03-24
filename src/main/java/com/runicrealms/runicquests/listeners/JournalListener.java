package com.runicrealms.runicquests.listeners;

import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.runiccharacters.api.events.CharacterLoadEvent;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.event.EventInventory;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

import java.util.ArrayList;
import java.util.Objects;

public class JournalListener implements Listener {

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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

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
        if (!(e.getClickedInventory().getType().equals(InventoryType.PLAYER))) return;

        EventInventory.openQuestGui(pl, 1);
        e.setCancelled(true);
    }

    @EventHandler
    public void onHearthstoneUse(PlayerInteractEvent e) {

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

    private ItemStack getQuestJournal() {
        ItemStack rune = new ItemStack(Material.BOOK);
        rune = AttributeUtil.addCustomStat(rune, "soulbound", "true");
        ItemMeta meta = rune.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Quest Journal");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.GOLD + "" + ChatColor.BOLD + "RIGHT CLICK");
        lore.add(ChatColor.GRAY + "To view your quests!");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.DARK_GRAY + "Soulbound");
        meta.setLore(lore);
        rune.setItemMeta(meta);
        return rune;
    }
}
