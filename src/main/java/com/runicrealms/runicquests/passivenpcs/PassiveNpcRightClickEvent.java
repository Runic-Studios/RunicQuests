package com.runicrealms.runicquests.passivenpcs;

import com.runicrealms.runicquests.event.custom.RightClickNpcEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PassiveNpcRightClickEvent implements Listener {

    @EventHandler
    public void onPassiveNpcRightClick(RightClickNpcEvent event) {

        Player player = event.getPlayer();

    }
}
