package com.runicrealms.runicquests.passivenpcs;

import com.runicrealms.plugin.api.NpcClickEvent;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.task.HologramTaskQueue;
import com.runicrealms.runicquests.task.TaskQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class PassiveNpcClickListener implements Listener {

    @EventHandler
    public void onNpcClick(NpcClickEvent event) {
        if(event.isCancelled()) return;
        PassiveNpc npc = RunicQuests.getPassiveNpcHandler().getNPC(event.getNpc().getId());
        if (npc == null) {
            return;
        }

        this.sendMessage(npc, event.getPlayer());
    }

    private void sendFirstMessage(PassiveNpc passiveNpc, Player player) {
        List<String> dialogue = passiveNpc.getDialogue();
        HologramTaskQueue hologramTaskQueue = new HologramTaskQueue
                (
                        HologramTaskQueue.QuestResponse.STARTED,
                        null,
                        null,
                        PassiveNpc.getPassiveNpcLocation(passiveNpc),
                        player,
                        dialogue
                );
        hologramTaskQueue.setCompletedTask(() -> {
            passiveNpc.TALKING.remove(player.getUniqueId());
            hologramTaskQueue.getHologram().delete();
        });
        passiveNpc.TALKING.put(player.getUniqueId(), hologramTaskQueue);
        hologramTaskQueue.startTasks();
    }

    private void sendMessage(PassiveNpc npc, Player player) {
        if (!npc.TALKING.containsKey(player.getUniqueId())) {
            this.sendFirstMessage(npc, player);
            return;
        }

        TaskQueue tasks = npc.TALKING.get(player.getUniqueId());

        tasks.nextTask();
        tasks.setDelay(RunicQuests.NPC_MESSAGE_DELAY);
    }
}
