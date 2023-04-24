package com.runicrealms.runicquests.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.NpcClickEvent;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.model.QuestProfileData;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestIdleMessage;
import com.runicrealms.runicquests.quests.QuestNpc;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;
import com.runicrealms.runicquests.task.HologramTaskQueue;
import com.runicrealms.runicquests.task.TaskQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Optional;

public class IdleMessageListener implements Listener {

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
        Player player = event.getPlayer();
        int slot = RunicCore.getCharacterAPI().getCharacterSlot(player.getUniqueId());
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
                handleIdleSpeech(player, quest, (QuestObjectiveTalk) objective, npcTaskQueues);
            }
        }
    }
}
