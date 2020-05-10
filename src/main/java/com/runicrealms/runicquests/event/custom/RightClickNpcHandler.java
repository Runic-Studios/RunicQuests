package com.runicrealms.runicquests.event.custom;

import com.runicrealms.runicnpcs.api.NpcClickEvent;
import com.runicrealms.runicquests.util.NpcPlugin;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RightClickNpcHandler implements Listener {

    @EventHandler
    public void onCitizensClick(NPCRightClickEvent event) {
        Bukkit.getPluginManager().callEvent(new RightClickNpcEvent(event.getClicker(), event.getNPC().getId(), NpcPlugin.CITIZENS));
    }

    @EventHandler
    public void onRunicNpcsClick(NpcClickEvent event) {
        Bukkit.getPluginManager().callEvent(new RightClickNpcEvent(event.getPlayer(), event.getNpc().getId(), NpcPlugin.RUNICNPCS));
    }

}
