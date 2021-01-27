package com.runicrealms.runicquests.passivenpcs;

import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicnpcs.api.NpcClickEvent;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.task.TaskQueue;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class PassiveNpcClickListener implements Listener {

    @EventHandler
    public void onNpcClick(NpcClickEvent event) {
        PassiveNpc npc = Plugin.getPassiveNpcHandler().getNPC(event.getNpc().getId());
        if (npc == null) {
            return;
        }

        this.sendMessage(npc, event.getPlayer());
    }

    @EventHandler
    private void onCitizensNpcClick(NPCRightClickEvent event) {
        PassiveNpc npc = Plugin.getPassiveNpcHandler().getNPC(event.getNPC().getId());
        if (npc == null) {
            return;
        }

        this.sendMessage(npc, event.getClicker());
    }

    private void sendMessage(PassiveNpc npc, Player player) {
        List<String> dialogue = npc.getDialogue();
        List<Runnable> runnables = new ArrayList<>();
        boolean override = npc.isOverrideText();

        for (String message : dialogue) {
            if (!override) {
                runnables.add(() -> player.sendMessage(ColorUtil.format("&7[" + (dialogue.indexOf(message) + 1) + "/" + dialogue.size() + "] &e" + npc.getName() + ": &6" + message)));
            } else {
                runnables.add(() -> player.sendMessage(ColorUtil.format("&6" + message)));
            }
        }

        new TaskQueue(runnables).startTasks();
    }
}
