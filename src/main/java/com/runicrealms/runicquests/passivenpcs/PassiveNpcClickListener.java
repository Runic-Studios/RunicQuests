package com.runicrealms.runicquests.passivenpcs;

import com.runicrealms.runicnpcs.api.NpcClickEvent;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.task.HologramTaskQueue;
import com.runicrealms.runicquests.task.TaskQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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

    private void sendMessage(PassiveNpc npc, Player player) {
        if (!npc.TALKING.containsKey(player.getUniqueId())) {
            this.sendFirstMessage(npc, player);
            return;
        }

        TaskQueue tasks = npc.TALKING.get(player.getUniqueId());

        tasks.nextTask();
        tasks.setDelay(Plugin.NPC_MESSAGE_DELAY);
    }

    private void sendFirstMessage(PassiveNpc passiveNpc, Player player) {
        List<String> dialogue = passiveNpc.getDialogue();
        HologramTaskQueue hologramTaskQueue = new HologramTaskQueue(HologramTaskQueue.QuestResponse.STARTED, null, PassiveNpc.getPassiveNpcLocation(passiveNpc), player, dialogue);
        hologramTaskQueue.setCompletedTask(() -> {
            passiveNpc.TALKING.remove(player.getUniqueId());
            hologramTaskQueue.getHologram().delete();
        });
        passiveNpc.TALKING.put(player.getUniqueId(), hologramTaskQueue);
        hologramTaskQueue.startTasks();
    }
}
