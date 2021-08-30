package com.runicrealms.runicquests.event.custom;

import com.runicrealms.runicnpcs.api.NpcClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RightClickNpcHandler implements Listener {

    @EventHandler
    public void onRunicNpcsClick(NpcClickEvent event) {
        Bukkit.getPluginManager().callEvent(new RightClickNpcEvent(event.getPlayer(), event.getNpc().getId()));
    }

}
