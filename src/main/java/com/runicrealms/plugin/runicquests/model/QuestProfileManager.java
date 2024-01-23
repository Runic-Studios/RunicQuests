package com.runicrealms.plugin.runicquests.model;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainAbortAction;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.api.WriteCallback;
import com.runicrealms.plugin.rdb.event.CharacterDeleteEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.rdb.event.CharacterSelectEvent;
import com.runicrealms.plugin.rdb.model.CharacterField;
import com.runicrealms.plugin.runicquests.RunicQuests;
import com.runicrealms.plugin.runicquests.api.QuestCompleteEvent;
import com.runicrealms.plugin.runicquests.api.QuestCompleteObjectiveEvent;
import com.runicrealms.plugin.runicquests.api.QuestStartEvent;
import com.runicrealms.plugin.runicquests.api.QuestWriteOperation;
import com.runicrealms.plugin.runicquests.api.RunicQuestsAPI;
import com.runicrealms.plugin.runicquests.config.QuestLoader;
import com.runicrealms.plugin.runicquests.quests.Quest;
import com.runicrealms.plugin.runicquests.quests.QuestObjectiveType;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjective;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveHandler;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveTalk;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveTrigger;
import com.runicrealms.plugin.runicquests.quests.trigger.Trigger;
import com.runicrealms.plugin.runicquests.quests.trigger.TriggerObjectiveHandler;
import com.runicrealms.plugin.runicquests.quests.trigger.TriggerType;
import com.runicrealms.plugin.runicquests.util.QuestsUtil;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Used to memoize quest data so that it is more performant
 * Also acts as a listener to keep redis up-to-date
 */
public class QuestProfileManager implements Listener, RunicQuestsAPI, QuestWriteOperation, QuestObjectiveHandler {
    public static final TaskChainAbortAction<Player, String, ?> CONSOLE_LOG = new TaskChainAbortAction<>() {
        public void onAbort(TaskChain<?> chain, Player player, String message) {
            Bukkit.getLogger().log(Level.SEVERE, ChatColor.translateAlternateColorCodes('&', message));
        }
    };
    private final Map<Object, QuestProfileData> questProfileDataMap; // keyed by uuid

    public QuestProfileManager() {
        questProfileDataMap = new HashMap<>();
        RunicQuests.getInstance().getServer().getPluginManager().registerEvents(this, RunicQuests.getInstance());
    }

    public Map<Object, QuestProfileData> getQuestProfileDataMap() {
        return questProfileDataMap;
    }

    @Override
    public List<Quest> getBlankQuestList() {
        return QuestLoader.getBlankQuestListRef();
    }

    @Override
    public QuestProfileData getQuestProfile(UUID uuid) {
        return this.getQuestProfileDataMap().get(uuid);
    }

    @Override
    public QuestProfileData loadQuestProfile(UUID uuid, int slot) {
        // Step 1: Check the mongo database
        Query query = new Query();
        query.addCriteria(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
        QuestProfileData result = mongoTemplate.findOne(query, QuestProfileData.class);
        if (result != null) {
            result.loadQuestsFromDTOs(); // Once we retrieve our object from Mongo, convert DTO to quests
            return result;
        }
        // Step 2: If no data is found, we create some data and save it to the collection
        QuestProfileData newData = new QuestProfileData(new ObjectId(), uuid, slot);
        newData.addDocumentToMongo();
        return newData;
    }

    @Override
    public boolean loadQuestsList(QuestProfileData profileData, int slot) {
        List<Quest> questList = profileData.getQuestsMap().get(slot);
        if (questList != null) return true;
        // No quests found for this character slot
        questList = QuestLoader.getQuestListNoUserData();
        profileData.getQuestsMap().put(slot, questList);
        return false;
    }

    @Override
    public boolean shouldWriteData(UUID uuid, Quest quest) {
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid);

        if (quest.isRepeatable()) {
            boolean isOnCooldown = true;
            if (RunicQuests.getQuestCooldowns().get(uuid) == null)
                isOnCooldown = false;
            else if (RunicQuests.getQuestCooldowns().get(uuid).get(slot).get(quest.getQuestID()) == null)
                isOnCooldown = false;
            if (quest.getQuestState().hasStarted() || quest.getQuestState().isCompleted() || isOnCooldown)
                return true;
        }
        return quest.getQuestState().hasStarted();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCharacterDelete(CharacterDeleteEvent event) {
        event.getPluginsToDeleteData().add("quests");
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int slot = event.getSlot();
        // Removes player from the save task
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
            jedis.srem(database + ":markedForSave:quests", String.valueOf(player.getUniqueId()));
            // 1. Delete from Redis
            jedis.srem(database + ":" + uuid + ":questData", String.valueOf(slot));
            // 2. Delete from Mongo
            Query query = new Query();
            query.addCriteria(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
            Update update = new Update();
            update.unset("questsDTOMap." + slot);
            MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
            mongoTemplate.updateFirst(query, update, QuestProfileData.class);
            // 3. Mark this deletion as complete
            event.getPluginsToDeleteData().remove("quests");
        }
    }

    /**
     * Remove quest data in-memory quests on character quit
     */
    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onCharacterQuit(CharacterQuitEvent event) {
        RunicQuests.getQuestCooldowns().remove(event.getPlayer().getUniqueId()); // Remove the cooldown object
        QuestProfileData questProfileData = questProfileDataMap.remove(event.getPlayer().getUniqueId()); // Get the quest profile
        for (Quest quest : questProfileData.getQuestsMap().get(event.getSlot())) { // Loop through the quests
            for (QuestObjective objective : quest.getObjectives()) { // Loop through objectives
                if (objective.getObjectiveType() == QuestObjectiveType.TALK) { // Check for objective of type talk
                    /*
                     * This is a minor bug fix which prevents minor issues with players
                     * talking to NPCs, then logging out
                     */
                    RunicQuests.getNpcTaskQueues().remove(((QuestObjectiveTalk) objective).getQuestNpc().getId());
                }
            }
        }
        RunicQuests.getLocationManager().getCachedLocations().remove(event.getPlayer());
    }

    /**
     * Ensure quest data is cached in redis on character select
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onCharacterSelect(CharacterSelectEvent event) {
        // For benchmarking
        long startTime = System.nanoTime();
        event.getPluginsToLoadData().add("quests");
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int slot = event.getSlot();
        TaskChain<?> chain = RunicQuests.newChain();
        chain
                .asyncFirst(() -> {
                    QuestProfileData profileData = loadQuestProfile(uuid, slot);
                    boolean questsFound = loadQuestsList(profileData, slot);
                    if (!questsFound) {
                        updateQuestProfileData
                                (
                                        profileData.getUuid(),
                                        slot,
                                        profileData,
                                        () -> {
                                        }
                                );
                    }
                    return profileData;
                })
                .abortIfNull(QuestProfileManager.CONSOLE_LOG, player, "RunicItems failed to load on select!")
                .syncLast(questProfileData -> {
                    questProfileDataMap.put(uuid, questProfileData); // Add to in-game memory
                    RunicQuests.getLocationManager().updatePlayerCachedLocations(event.getPlayer(), event.getSlot());
                    event.getPluginsToLoadData().remove("quests");
                    // Calculate elapsed time
                    long endTime = System.nanoTime();
                    long elapsedTime = endTime - startTime;
                    // Log elapsed time in milliseconds
                    Bukkit.getLogger().info("RunicQuests took: " + elapsedTime / 1_000_000 + "ms to load");
                })
                .execute();
    }

    /**
     * Saves player skill tree info when the server is shut down
     * for EACH alt the player has used during the runtime of this server.
     * Works even if the player is now entirely offline
     */
    @EventHandler
    public void onDatabaseSave(MongoSaveEvent event) {
        event.markPluginSaved("quests");
    }

    @EventHandler(priority = EventPriority.HIGHEST) // late
    public void onQuestComplete(QuestCompleteEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid);
        QuestProfileData questProfileData = this.getQuestProfile(uuid);

        updateQuestProfileData
                (
                        uuid,
                        slot,
                        questProfileData,
                        () -> {
                        }
                );
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuestStart(QuestStartEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid);
        QuestProfileData questProfileData = this.getQuestProfile(uuid);
        updateQuestProfileData
                (
                        uuid,
                        slot,
                        questProfileData,
                        () -> {
                        }
                );
    }

    @EventHandler
    public void onQuestCompleteObjective(QuestCompleteObjectiveEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid);
        QuestProfileData questProfileData = this.getQuestProfile(uuid);
        updateQuestProfileData
                (
                        uuid,
                        slot,
                        questProfileData,
                        () -> {
                        }
                );
    }

    @Override
    public void updateQuestProfileData(UUID uuid, int slot, QuestProfileData questProfileData, WriteCallback callback) {
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
        TaskChain<?> chain = RunicQuests.newChain();
        chain
                .asyncFirst(() -> {
                    // Prepare a new map of slot to data transfer object
                    Map<Integer, QuestDTO> questDTOMap = QuestProfileData.getBlankQuestDTOMap();
                    questProfileData.getQuestsMap().get(slot).forEach(quest -> {
                        QuestDTO data = new QuestDTO(quest);

                        Date completedOn = QuestsUtil.getCompletedDate(uuid, quest);
                        if (completedOn != null) {
                            data.setCompletedDate(completedOn);
                        }

                        questDTOMap.put(quest.getQuestID(), data);
                    });
                    questProfileData.getQuestsDTOMap().put(slot, questDTOMap);

                    // Define a query to find the InventoryData for this player
                    Query query = new Query();
                    query.addCriteria(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));

                    // Define an update to set the specific field
                    Update update = new Update();
                    update.set("questsDTOMap." + slot, questProfileData.getQuestsDTOMap().get(slot));

                    // Execute the update operation
                    return mongoTemplate.updateFirst(query, update, QuestProfileData.class);
                })
                .abortIfNull(CONSOLE_LOG, null, "RunicQuests failed to write to questsDTOMap!")
                .syncLast(updateResult -> callback.onWriteComplete())
                .execute();
    }

    @Override
    public void triggerQuest(boolean applyToParty, @NotNull Player player, @NotNull String triggerId, @Nullable String objectiveName) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicQuests.getInstance(), () -> {
            Set<Player> players = new HashSet<>();
            players.add(player);

            if (applyToParty) {
                addPartyMembersToTriggerCredit(player, players);
            }

            Trigger trigger = TriggerObjectiveHandler.getTrigger(triggerId);
            if (trigger == null) {
                return;
            }

            for (Player playerToReceiveCredit : players) {
                handleTrigger(playerToReceiveCredit, trigger, objectiveName);
            }
        });
    }

    /**
     * @param player  who caused the trigger
     * @param players a set of players to receive credit from the trigger
     */
    private void addPartyMembersToTriggerCredit(Player player, Set<Player> players) {
        Party party = RunicCore.getPartyAPI().getParty(player.getUniqueId());
        if (party != null) {
            players.addAll(party.getMembersWithLeader());
        } else {
            players.add(player);
        }
    }

    /**
     * @param player        who caused trigger
     * @param trigger       that was caused
     * @param objectiveName the name of the objective that should be sent to the player in the progress message
     */
    private void handleTrigger(@NotNull Player player, @NotNull Trigger trigger, @Nullable String objectiveName) {
        int characterSlot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
        QuestProfileData profileData = RunicQuests.getAPI().getQuestProfile(player.getUniqueId());
        for (Quest quest : profileData.getQuestsMap().get(characterSlot)) {
            if (!isQuestActive(quest)) continue;
            if (quest.getQuestID() != trigger.getQuestId())
                continue; // Find the quest associated with trigger
            QuestObjectiveTrigger triggerObjective = (QuestObjectiveTrigger) QuestObjective.getObjective(quest.getObjectives(), trigger.getObjectiveId());
            if (triggerObjective == null) continue;
            for (QuestObjective objective : quest.getObjectives()) {
                // Ensure our objective is current and valid
                if (!isValidObjective(quest, objective, QuestObjectiveType.TRIGGER)) continue;
                // Ensure that our current objective matches the trigger objective
                if (!objective.getObjectiveNumber().equals(triggerObjective.getObjectiveNumber()))
                    continue;
                incrementTriggerObjective(player, trigger, profileData, quest, triggerObjective, objectiveName);
            }
        }
    }

    private void incrementTriggerObjective(Player player, Trigger trigger, QuestProfileData profileData, Quest quest, QuestObjectiveTrigger triggerObjective, @Nullable String objectiveName) {
        Set<String> triggersEarned = triggerObjective.getTriggersEarned();
        // How many triggers for this objective did they have before this one?
        int previousEarnedTriggerCount = triggersEarned.size();
        triggersEarned.add(trigger.getTriggerId());
        // If the player has achieved all triggers
        if (triggerObjective.getTriggerType() == TriggerType.ANY || triggersEarned.size() >= triggerObjective.getTriggerIds().size()) {
            // Handle trigger SYNC
            Bukkit.getScheduler().runTask(RunicQuests.getInstance(), () -> progressQuest(player, profileData, quest, triggerObjective));
        } else if (triggersEarned.size() > previousEarnedTriggerCount) { // Player discovered a unique trigger (set doesn't allow duplicates)
            player.sendMessage(ChatColor.translateAlternateColorCodes
                    ('&', QuestsUtil.PREFIX + " " + (objectiveName != null ? objectiveName : "Hidden Trigger") + " &6» &7[&a" + triggerObjective.getTriggersEarned().size() + "&7/" + triggerObjective.getTriggerIds().size() + "]"));
        }
    }
}
