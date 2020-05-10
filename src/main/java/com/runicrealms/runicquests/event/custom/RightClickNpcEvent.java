package com.runicrealms.runicquests.event.custom;

import com.runicrealms.runicquests.util.NpcPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RightClickNpcEvent extends Event {

    private Player player;
    private Integer npcId;
    private NpcPlugin plugin;

    private static final HandlerList handlers = new HandlerList();

    public RightClickNpcEvent(Player player, Integer npcId, NpcPlugin plugin) {
        this.player = player;
        this.npcId = npcId;
        this.plugin = plugin;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Integer getNpcId() {
        return this.npcId;
    }

    public NpcPlugin getPlugin() {
        return this.plugin;
    }
}