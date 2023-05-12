package com.runicrealms.runicquests.model;

import com.runicrealms.libs.taskchain.TaskChain;
import com.runicrealms.libs.taskchain.TaskChainAbortAction;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterDeleteEvent;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.plugin.model.CharacterField;
import com.runicrealms.plugin.model.SessionDataNested;
import com.runicrealms.plugin.model.SessionDataNestedManager;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.api.QuestCompleteEvent;
import com.runicrealms.runicquests.api.QuestCompleteObjectiveEvent;
import com.runicrealms.runicquests.api.QuestStartEvent;
import com.runicrealms.runicquests.api.RunicQuestsAPI;
import com.runicrealms.runicquests.config.QuestLoader;
import com.runicrealms.runicquests.quests.Quest;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.logging.Level;

/**
 * Used to memoize quest data so that it is more performant
 * Also acts as a listener to keep redis up-to-date
 */
public class QuestProfileManager implements Listener, RunicQuestsAPI, SessionDataNestedManager {
    public static final TaskChainAbortAction<Player, String, ?> CONSOLE_LOG = new TaskChainAbortAction<>() {
        public void onAbort(TaskChain<?> chain, Player player, String message) {
            Bukkit.getLogger().log(Level.SEVERE, ChatColor.translateAlternateColorCodes('&', message));
        }
    };
    private final Map<Object, SessionDataNested> questProfileDataMap; // keyed by uuid

    public QuestProfileManager() {
        questProfileDataMap = new HashMap<>();
        RunicQuests.getInstance().getServer().getPluginManager().registerEvents(this, RunicQuests.getInstance());
    }

    /**
     * For the given character slot, creates a list of quests. For quests with data in redis,
     * populates that data in-memory
     *
     * @param uuid of the player
     * @param slot of the character
     * @return a list of quests to be used during the session with written data
     */
    public List<Quest> buildQuestListFromRedis(UUID uuid, Jedis jedis, int slot) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        // Populates the in-memory quest data from redis
        String parentKey = QuestProfileData.getJedisKey(uuid, slot);
        List<Quest> questListNoUserData = QuestLoader.getQuestListNoUserData();
        List<Quest> result = new ArrayList<>();
        try {
            for (Quest questNoUserData : questListNoUserData) {
                if (jedis.exists(database + ":" + parentKey + ":" + questNoUserData.getQuestID())) {
                    Map<String, String> questDataMap = jedis.hgetAll(database + ":" + parentKey + ":" + questNoUserData.getQuestID()); // get the parent key of the section
                    Quest questWithUserData = QuestProfileData.writeUserDataToQuest(uuid, questNoUserData, questDataMap, questNoUserData.getQuestID() + "");
                    result.add(questWithUserData);
                } else {
                    result.add(questNoUserData);
                }
            }
        } catch (Exception ex) {
            Bukkit.getLogger().severe("There was a problem building quests list from Redis!");
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Checks redis to see if the currently selected character's quest profile is cached.
     * And if it is, returns the QuestProfileData object
     *
     * @param object the uuid of player to check
     * @param slot   of the character
     * @param jedis  the jedis resource
     * @return a QuestProfileData object if it is found in redis
     */
    @Override
    public SessionDataNested checkJedisForSessionData(Object object, Jedis jedis, int... slot) {
        return null;
    }

    /**
     * Creates a QuestProfileData object.
     * Tries to build it from memoization, then from session storage (Redis),
     * then falls back to Mongo
     *
     * @param object the uuid of player who is attempting to load their data
     * @param slot   the slot of the character
     */
    @Override
    public SessionDataNested getSessionData(Object object, int... slot) {
        UUID uuid = (UUID) object;
        // Check if data is memoized
        return questProfileDataMap.get(uuid);
    }

    @Override
    public Map<Object, SessionDataNested> getSessionDataMap() {
        return questProfileDataMap;
    }

    @Override
    public SessionDataNested loadSessionData(Object object, int... slotToLoad) {
        UUID uuid = (UUID) object;
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            // Step 1: Check if quest data is cached in redis
            Set<String> redisQuestList = RunicCore.getRedisAPI().getRedisDataSet(uuid, "questData", jedis);
            boolean dataInRedis = RunicCore.getRedisAPI().determineIfDataInRedis(redisQuestList, slotToLoad[0]);
            if (dataInRedis) {
                return new QuestProfileData(null, uuid, jedis, slotToLoad[0]);
            }
            // Step 2: Check the mongo database
            Query query = new Query();
            query.addCriteria(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
            MongoTemplate mongoTemplate = RunicCore.getDataAPI().getMongoTemplate();
            List<QuestProfileData> results = mongoTemplate.find(query, QuestProfileData.class);
            if (results.size() > 0) {
                QuestProfileData result = results.get(0);
                result.loadQuestsFromDTOs(); // Once we retrieve our object from Mongo, convert DTO to quests
                result.writeToJedis(jedis);
                return result;
            }
            // Step 3: If no data is found, we create some data and save it to the collection
            QuestProfileData newData = new QuestProfileData(new ObjectId(), uuid, slotToLoad[0]);
            newData.addDocumentToMongo();
            newData.writeToJedis(jedis);
            return newData;
        }
    }

    @Override
    public List<Quest> getBlankQuestList() {
        return QuestLoader.getBlankQuestListRef();
    }

    @Override
    public QuestProfileData getQuestProfile(UUID uuid) {
        return (QuestProfileData) this.getSessionData(uuid);
    }

    @Override
    public List<Quest> loadQuestsList(QuestProfileData profileData, int slot) {
        List<Quest> questList = profileData.getQuestsMap().get(slot);
        if (questList != null) return questList;
        questList = QuestLoader.getQuestListNoUserData();
        profileData.getQuestsMap().put(slot, questList);
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            profileData.writeToJedis(jedis, slot);
        }
        return profileData.getQuestsMap().get(slot);
    }

    @Override
    public boolean shouldWriteData(UUID uuid, Quest quest) {
        if (quest.isRepeatable()) {
            boolean isOnCooldown = true;
            if (RunicQuests.getQuestCooldowns().get(uuid) == null)
                isOnCooldown = false;
            else if (RunicQuests.getQuestCooldowns().get(uuid).get(quest.getQuestID()) == null)
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
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            String database = RunicCore.getDataAPI().getMongoDatabase().getName();
            jedis.srem(database + ":markedForSave:quests", String.valueOf(player.getUniqueId()));
        }
        // 1. Delete from Redis
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            jedis.srem(database + ":" + uuid + ":questData", String.valueOf(slot));
        }
        // 2. Delete from Mongo
        Query query = new Query();
        query.addCriteria(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
        Update update = new Update();
        update.unset("questsDTOMap." + slot);
        MongoTemplate mongoTemplate = RunicCore.getDataAPI().getMongoTemplate();
        mongoTemplate.updateFirst(query, update, QuestProfileData.class);
        // 3. Mark this deletion as complete
        event.getPluginsToDeleteData().remove("quests");
    }

    /**
     * Remove quest data in-memory quests on character quit
     */
    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onCharacterQuit(CharacterQuitEvent event) {
        questProfileDataMap.remove(event.getPlayer().getUniqueId());
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
                    QuestProfileData profileData = (QuestProfileData) loadSessionData(uuid, slot);
                    loadQuestsList(profileData, slot);
                    return profileData;
                })
                .abortIfNull(QuestProfileManager.CONSOLE_LOG, player, "RunicItems failed to load on select!")
                .syncLast(questProfileData -> {
                    getSessionDataMap().put(uuid, questProfileData); // add to in-game memory
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
        // Cancel the task timer
        RunicQuests.getMongoTask().getTask().cancel();
        // Manually save all data (flush players marked for save)
        RunicQuests.getMongoTask().saveAllToMongo(() -> event.markPluginSaved("quests"));
    }

    @EventHandler(priority = EventPriority.HIGHEST) // late
    public void onQuestComplete(QuestCompleteEvent event) {
        QuestProfileData questProfileData = (QuestProfileData) this.getSessionData(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                questProfileData.writeToJedis(jedis, RunicCore.getCharacterAPI().getCharacterSlot(event.getPlayer().getUniqueId()));
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuestStart(QuestStartEvent event) {
        QuestProfileData questProfileData = (QuestProfileData) this.getSessionData(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                questProfileData.writeToJedis(jedis, RunicCore.getCharacterAPI().getCharacterSlot(event.getPlayer().getUniqueId()));
            }
        });
    }

    @EventHandler
    public void onQuestStart(QuestCompleteObjectiveEvent event) {
        QuestProfileData questProfileData = (QuestProfileData) this.getSessionData(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                questProfileData.writeToJedis(jedis, RunicCore.getCharacterAPI().getCharacterSlot(event.getPlayer().getUniqueId()));
            }
        });
    }

}
