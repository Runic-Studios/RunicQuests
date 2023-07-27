package com.runicrealms.plugin.runicquests.listeners;

import com.runicrealms.plugin.npcs.api.NpcClickEvent;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.runicquests.model.QuestProfileData;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjective;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveTalk;
import com.runicrealms.plugin.runicquests.task.HologramTaskQueue;
import com.runicrealms.plugin.runicquests.task.TaskQueue;
import com.runicrealms.plugin.runicquests.RunicQuests;
import com.runicrealms.plugin.runicquests.quests.Quest;
import com.runicrealms.plugin.runicquests.quests.QuestIdleMessage;
import com.runicrealms.plugin.runicquests.quests.QuestNpc;
import com.runicrealms.plugin.runicquests.quests.QuestObjectiveType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class IdleMessageListener implements Listener {

    private final Map<UUID, Long> lastClickedNPCs = new HashMap<>();

    private void handleIdleSpeech(Player player, Quest quest, QuestObjectiveTalk talkObjective, HashMap<Long, TaskQueue> npcTaskQueues) {
        outerLoop:
        for (QuestIdleMessage idleMessage : talkObjective.getQuestNpc().getIdleSpeech()) { // Loop through idle messages
            if (idleMessage.getConditions().hasQuestCompleted()) { // Check for quest completed condition
                if (quest.getQuestState().isCompleted() != idleMessage.getConditions().getQuestCompleted()) {
                    continue;
                }
            }
            if (idleMessage.getConditions().hasQuestStarted()) { // Check for quest started condition
                if (quest.getQuestState().hasStarted() != idleMessage.getConditions().getQuestStarted()) {
                    continue;
                }
            }
            if (idleMessage.getConditions().hasObjectiveStates()) { // Check for objectives completed condition
                for (int i = 1; i <= quest.getObjectives().size(); i++) { // for every quest objective
                    if (idleMessage.getConditions().getObjectiveStates().size() - 1 >= i) {
                        if (idleMessage.getConditions().getObjectiveStates().get(i) != null) {
                            int finalI = i;
                            Optional<QuestObjective> questObjective = quest.getObjectives().stream().filter(obj -> obj.getObjectiveNumber() == finalI).findFirst();
                            if (questObjective.isPresent() && questObjective.get().isCompleted() != idleMessage.getConditions().getObjectiveStates().get(i)) {
                                continue outerLoop;
                            }
                        }
                    }
                }
            }
            if (talkObjective.requiresQuestItem()) {
                if (idleMessage.getConditions().hasRequiresQuestItems()) { // Check for requires quest items condition
                    if (RunicQuests.hasQuestItems(talkObjective, player) != idleMessage.getConditions().requiresQuestItems()) {
                        continue;
                    }
                }
            }
            // If the player is currently talking with the NPC, simply move to next speech line
            if (npcTaskQueues.containsKey(talkObjective.getQuestNpc().getId())) {
                npcTaskQueues.get(talkObjective.getQuestNpc().getId()).nextTask();
                return;
            }
            HologramTaskQueue queue = new HologramTaskQueue
                    (
                            HologramTaskQueue.QuestResponse.IDLE,
                            quest,
                            talkObjective.getQuestNpc().getNpcId(),
                            QuestNpc.getQuestNpcLocation(talkObjective.getQuestNpc()),
                            player,
                            idleMessage.getSpeech()
                    );
            queue.setCompletedTask(() -> npcTaskQueues.remove(talkObjective.getQuestNpc().getId()));
            npcTaskQueues.put(talkObjective.getQuestNpc().getId(), queue);
            queue.startTasks();
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onNpcClick(NpcClickEvent event) {
        if(event.isCancelled()) return;
        Player player = event.getPlayer();
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
        QuestProfileData profileData = RunicQuests.getAPI().getQuestProfile(player.getUniqueId());
        // Static map that keeps track of the current talk operation
        HashMap<Long, TaskQueue> npcTaskQueues = RunicQuests.getNpcTaskQueues();
        for (Quest quest : profileData.getQuestsMap().get(slot)) {
            for (QuestObjective objective : quest.getObjectives()) { // Check every objective for an idle message
                if (objective.getObjectiveType() != QuestObjectiveType.TALK) continue;
                QuestObjectiveTalk talkObjective = (QuestObjectiveTalk) objective;
                if (!talkObjective.getQuestNpc().getNpcId().equals(event.getNpc().getId()))
                    continue;
                if (!talkObjective.getQuestNpc().hasIdleSpeech()) continue;
                Long lastTalked = lastClickedNPCs.get(player.getUniqueId());
                if (lastTalked != null && System.currentTimeMillis() - lastTalked <= 250) return;
                lastClickedNPCs.put(player.getUniqueId(), System.currentTimeMillis());
                handleIdleSpeech(player, quest, (QuestObjectiveTalk) objective, npcTaskQueues);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastClickedNPCs.remove(event.getPlayer().getUniqueId());
    }
}
