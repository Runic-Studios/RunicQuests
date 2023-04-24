package com.runicrealms.runicquests.listeners;

import com.runicrealms.plugin.character.api.CharacterLoadedEvent;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicquests.ui.CompassManager;
import com.runicrealms.runicquests.ui.QuestMenu;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
    public void onCharacterLoad(CharacterLoadedEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        player.getInventory().setItem(7, getQuestJournal());
        player.updateInventory();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() != InventoryType.PLAYER) return;
        Player player = (Player) event.getWhoClicked();
        int itemSlot = event.getSlot();
        if (itemSlot != 7) return;

        // don't trigger if there's no item in the slot to avoid null issues
        if (player.getInventory().getItem(7) == null) return;
        ItemStack book = player.getInventory().getItem(7);

        ItemMeta meta = Objects.requireNonNull(book).getItemMeta();
        if (meta == null) return;

        // only activate in survival mode to save builders the headache
        if (player.getGameMode() == GameMode.CREATIVE) return;

        // only listen for a player inventory
        if (event.getClickedInventory() == null) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuestJournalUse(PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Event.Result.DENY && event.useItemInHand() == Event.Result.DENY)
            return;
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        int slot = player.getInventory().getHeldItemSlot();
        if (slot != 7) return;
        // annoying 1.9 feature which makes the event run twice, once for each hand
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            player.openInventory(new QuestMenu(player).getInventory());
            event.setCancelled(true);
        } else if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
                && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.COMPASS
                && CompassManager.getCompasses().containsKey(event.getPlayer())
                && CompassManager.getCompasses().get(event.getPlayer()).getLocation() != null) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            CompassManager.CompassLocation comp = CompassManager.getCompasses().get(event.getPlayer());
            comp.send(player);
            event.setCancelled(true);
        }
    }
}
