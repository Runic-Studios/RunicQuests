package com.runicrealms.runicquests.event.custom;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RightClickNpcEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Integer npcId;

    public RightClickNpcEvent(Player player, Integer npcId) {
        this.player = player;
        this.npcId = npcId;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Integer getNpcId() {
        return this.npcId;
    }
}