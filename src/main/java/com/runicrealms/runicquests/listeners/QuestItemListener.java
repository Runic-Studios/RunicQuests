package com.runicrealms.runicquests.listeners;

import com.runicrealms.api.event.ChatChannelMessageEvent;
import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicquests.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestItemListener implements Listener {

    private static final int CHAT_TIMEOUT = 15; // seconds
    private final Map<UUID, ItemDestroyIntent> itemDestroyers;

    public QuestItemListener() {
        itemDestroyers = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.LOWEST) // runs first
    public void onItemDrop(PlayerDropItemEvent e) {
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(e.getItemDrop().getItemStack());
        if (runicItem == null) return;
        if (!RunicItemsAPI.containsBlockedTag(runicItem)) return;
        if (runicItem.getTags().contains(RunicItemTag.SOULBOUND)) return; // handled somewhere else
        e.setCancelled(true);
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
        e.getPlayer().sendMessage(ChatColor.RED + "This item will be destroyed. Are you sure? Type YES or NO");
        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                itemDestroyers.remove(e.getPlayer().getUniqueId());
            }
        }.runTaskLater(Plugin.getInstance(), CHAT_TIMEOUT * 20L);
        itemDestroyers.put(e.getPlayer().getUniqueId(), new ItemDestroyIntent(bukkitTask, e.getItemDrop().getItemStack()));
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onChat(ChatChannelMessageEvent e) {
        if (!itemDestroyers.containsKey(e.getMessageSender().getUniqueId())) return;
        e.setCancelled(true);
        if (e.getChatMessage().equalsIgnoreCase("yes")) {
            itemDestroyers.get(e.getMessageSender().getUniqueId()).getBukkitTask().cancel();
            ItemRemover.takeItem
                    (
                            e.getMessageSender(),
                            itemDestroyers.get(e.getMessageSender().getUniqueId()).getItemStack(),
                            itemDestroyers.get(e.getMessageSender().getUniqueId()).getItemStack().getAmount()
                    );
            itemDestroyers.remove(e.getMessageSender().getUniqueId());
            e.getMessageSender().playSound(e.getMessageSender().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1.0f);
            e.getMessageSender().sendMessage(ChatColor.YELLOW + "The item has been destroyed!");
        } else if (e.getChatMessage().equalsIgnoreCase("no")) {
            itemDestroyers.get(e.getMessageSender().getUniqueId()).getBukkitTask().cancel();
            itemDestroyers.remove(e.getMessageSender().getUniqueId());
            e.getMessageSender().sendMessage(ChatColor.YELLOW + "The item was not destroyed.");
        } else {
            e.getMessageSender().sendMessage(ChatColor.YELLOW + "You are attempting to destroy an item. Type YES or NO");
        }
    }

    /**
     * Helper class to manage chat task
     */
    static class ItemDestroyIntent {

        private final BukkitTask bukkitTask;
        private final ItemStack itemStack;

        public ItemDestroyIntent(BukkitTask bukkitTask, ItemStack itemStack) {
            this.bukkitTask = bukkitTask;
            this.itemStack = itemStack;
        }

        public BukkitTask getBukkitTask() {
            return bukkitTask;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }
    }
}
