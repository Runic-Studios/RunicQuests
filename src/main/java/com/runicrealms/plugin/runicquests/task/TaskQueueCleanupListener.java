package com.runicrealms.plugin.runicquests.task;

import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.runicquests.RunicQuests;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Removes all 'floating' hologram dialogue upon logout.
 * Intended to deal with memory leaks and frozen dialogue
 */
public class TaskQueueCleanupListener implements Listener {

    public static final Map<UUID, List<HologramTaskQueue>> CURRENT_TASK_QUEUES = new HashMap<>();

    public static List<HologramTaskQueue> getCurrentQueuesForUuid(UUID uuid) {
        List<HologramTaskQueue> currentTaskQueues = TaskQueueCleanupListener.CURRENT_TASK_QUEUES.get(uuid);
        if (currentTaskQueues == null)
            currentTaskQueues = new ArrayList<>();
        return currentTaskQueues;
    }

    @EventHandler
    public void onCharacterQuit(CharacterQuitEvent event) {
        if (!CURRENT_TASK_QUEUES.containsKey(event.getPlayer().getUniqueId())) return;
        UUID uuid = event.getPlayer().getUniqueId();
        List<HologramTaskQueue> taskQueues = CURRENT_TASK_QUEUES.get(uuid);
        Bukkit.getScheduler().runTask(RunicQuests.getInstance(), () -> { // Run task SYNC
            for (HologramTaskQueue taskQueue : taskQueues) {
                taskQueue.cancel();
            }
            CURRENT_TASK_QUEUES.remove(uuid);
        });
    }
}
