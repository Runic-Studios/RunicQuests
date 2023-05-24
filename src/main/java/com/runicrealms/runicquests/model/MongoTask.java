package com.runicrealms.runicquests.model;

import co.aikar.taskchain.TaskChain;
import com.mongodb.bulk.BulkWriteResult;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.api.MongoTaskOperation;
import com.runicrealms.plugin.rdb.api.WriteCallback;
import com.runicrealms.runicitems.model.InventoryDataManager;
import com.runicrealms.runicquests.RunicQuests;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Update;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.UUID;

/**
 * Manages the task that writes data from Redis --> MongoDB periodically
 *
 * @author Skyfallin
 */
public class MongoTask implements MongoTaskOperation {
    private static final int MONGO_TASK_TIME = 30; // seconds
    private final BukkitTask task;

    public MongoTask() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously
                (
                        RunicQuests.getInstance(),
                        () -> saveAllToMongo(() -> {
                        }),
                        MONGO_TASK_TIME * 20L,
                        MONGO_TASK_TIME * 20L
                );
    }

    @Override
    public String getCollectionName() {
        return "quests";
    }

    @Override
    public <T> Update getUpdate(T obj) {
        QuestProfileData data = (QuestProfileData) obj;
        Update update = new Update();
        /*
        Only update keys in mongo with quest data in memory.
        If, for example, there's 5 characters with data in mongo but only 1 in redis,
        this only updates the character with new data.
         */
        for (Integer slot : data.getQuestsDTOMap().keySet()) {
            update.set("questsDTOMap." + slot, data.getQuestsDTOMap().get(slot));
        }
        return update;
    }

    @Override
    public void saveAllToMongo(WriteCallback callback) {
        TaskChain<?> chain = RunicQuests.newChain();
        chain
                .asyncFirst(this::sendBulkOperation)
                .abortIfNull(InventoryDataManager.CONSOLE_LOG, null, "RunicQuests failed to write to Mongo!")
                .syncLast(bulkWriteResult -> {
                    if (bulkWriteResult.wasAcknowledged()) {
                        Bukkit.getLogger().info("RunicQuests modified " + bulkWriteResult.getModifiedCount() + " documents.");
                    }
                    callback.onWriteComplete();
                })
                .execute();
    }

    @Override
    public BulkWriteResult sendBulkOperation() {
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            Set<String> playersToSave = jedis.smembers(getJedisSet());
            if (playersToSave.isEmpty()) return BulkWriteResult.unacknowledged();
            BulkOperations bulkOperations = RunicDatabase.getAPI().getDataAPI().getMongoTemplate().bulkOps(BulkOperations.BulkMode.UNORDERED, getCollectionName());
            for (String uuidString : playersToSave) {
                UUID uuid = UUID.fromString(uuidString);
                QuestProfileData questProfileData = (QuestProfileData) RunicQuests.getAPI().loadSessionData(uuid, -1); // All slots
                questProfileData.syncForMongo(jedis); // Prepares the DTOs
                // Player is no longer marked for save
                jedis.srem(getJedisSet(), uuid.toString());
                // Find the correct document to update
                bulkOperations.updateOne(getQuery(uuid), getUpdate(questProfileData));
            }
            return bulkOperations.execute();
        }
    }

    public BukkitTask getTask() {
        return task;
    }

}
