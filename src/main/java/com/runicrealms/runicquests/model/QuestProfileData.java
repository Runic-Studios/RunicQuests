package com.runicrealms.runicquests.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.SessionDataMongo;
import com.runicrealms.plugin.model.SessionDataNested;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.config.QuestLoader;
import com.runicrealms.runicquests.quests.FirstNpcState;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.util.QuestsUtil;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import redis.clients.jedis.Jedis;

import java.util.*;

@Document(collection = "quests")
@SuppressWarnings("unused")
public class QuestProfileData implements SessionDataMongo, SessionDataNested {
    public static final List<String> FIELDS = new ArrayList<String>() {{
        add(QuestField.COMPLETED.getField());
        add(QuestField.DATE_COMPLETED.getField());
        add(QuestField.FIRST_NPC_STATE.getField());
        add(QuestField.STARTED.getField());
    }};
    public static final String PATH_LOCATION = "quests";
    public static final String DATA_LOCATION = "data";
    @Id
    private ObjectId id;
    @Field("playerUuid")
    private UUID uuid;
    /*
    Our main Data Transfer Object map.
    Keyed by player UUID, and the inner map is keyed by the quest's ID
    Used for persistent storage in MongoDB
     */
    private HashMap<Integer, HashMap<Integer, QuestDTO>> questsDTOMap = new HashMap<>();
    @Transient
    private HashMap<Integer, List<Quest>> questsMap = new HashMap<>(); // Keyed by character slot, not persisted to MongoDB

    @SuppressWarnings("unused")
    public QuestProfileData() {
        // Default constructor for Spring
    }

    /**
     * Constructor for new players
     */
    public QuestProfileData(ObjectId id, UUID uuid, int slot) {
        this.id = id;
        this.uuid = uuid;
        this.questsDTOMap.put(slot, getBlankQuestDTOMap());
        this.questsMap = new HashMap<Integer, List<Quest>>() {{
            put(slot, QuestLoader.getQuestListNoUserData());
        }};
        RunicQuests.getQuestCooldowns().put(this.uuid, new HashMap<>());
    }

    /**
     * Build the character's quest profile data from jedis
     *
     * @param uuid       of the player
     * @param jedis      the jedis resource
     * @param slotToLoad a character to load. -1 if it should load all characters
     */
    public QuestProfileData(ObjectId id, UUID uuid, Jedis jedis, int slotToLoad) {
        this.id = id;
        this.uuid = uuid;
        this.questsMap = new HashMap<>();
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        // Populate quests map with redis data
        if (slotToLoad == -1) { // Load all slots
            for (int slot = 1; slot <= RunicCore.getDataAPI().getMaxCharacterSlot(); slot++) {
                if (jedis.smembers(database + ":" + uuid + ":questData").contains(String.valueOf(slot))) {
                    List<Quest> questList = RunicQuests.getAPI().buildQuestListFromRedis(uuid, jedis, slot);
                    this.questsMap.put(slot, questList);
                }
            }
        } else {
            List<Quest> questList = RunicQuests.getAPI().buildQuestListFromRedis(uuid, jedis, slotToLoad);
            this.questsMap.put(slotToLoad, questList);
        }

        Player player = Bukkit.getPlayer(uuid);
        // Additional checks if player is online
        if (player == null) return;
        int slot = RunicCore.getCharacterAPI().getCharacterSlot(uuid);
        // This ensures there is blank data if the player has redis data, but no data for the current character
        if (this.getQuestsMap().get(slot) == null)
            this.questsMap.put(slot, QuestLoader.getQuestListNoUserData());
    }

    /**
     * @return a list of DTOs with 'blank' data (all quests not started)
     */
    public static HashMap<Integer, QuestDTO> getBlankQuestDTOMap() {
        HashMap<Integer, QuestDTO> questDTOMap = new HashMap<>();
        for (Quest quest : QuestLoader.getQuestListNoUserData()) {
            questDTOMap.put(quest.getQuestID(), new QuestDTO(quest));
        }
        return questDTOMap;
    }

    /**
     * Quests data is nested in redis, so here's a handy method to get the key
     *
     * @param uuid of the player
     * @param slot of the character
     * @return a string representing the location in jedis
     */
    public static String getJedisKey(UUID uuid, int slot) {
        return uuid + ":character:" + slot + ":" + PATH_LOCATION + ":" + DATA_LOCATION;
    }

    /**
     * We build an object of QuestProfileData from a blank 'master' list of quests.
     * Then, we populate each blank quest with data from the progress of the player.
     * This version loads from Redis
     *
     * @param questNoUserData the blank quest
     * @param questDataMap    a data map of the redis fields pertaining to the quest
     * @param questId         the id of the quest
     * @return the quest with user data
     */
    public static Quest writeUserDataToQuest(UUID uuid, Quest questNoUserData, Map<String, String> questDataMap, String questId) {
        RunicQuests.getQuestCooldowns().computeIfAbsent(uuid, k -> new HashMap<>());
        Map<Integer, Date> cooldowns = RunicQuests.getQuestCooldowns().get(uuid);
        Quest questWithUserData = new Quest(questNoUserData); // Clone a new quest with blank data
        questWithUserData.getQuestState().setCompleted(Boolean.parseBoolean(questDataMap.get(QuestField.COMPLETED.getField())));
        questWithUserData.getFirstNPC().setState(FirstNpcState.fromString(questDataMap.get(QuestField.FIRST_NPC_STATE.getField())));
        questWithUserData.getQuestState().setStarted(Boolean.parseBoolean(questDataMap.get(QuestField.STARTED.getField())));
        for (String field : questDataMap.keySet()) {
            for (QuestObjective questObjective : questNoUserData.getObjectives()) {
                if (QuestField.getFromFieldString(field) != null)
                    continue; // Ignore started, completed, etc.
                if (field.equalsIgnoreCase("objectives:" + questObjective.getObjectiveNumber()))
                    questObjective.setCompleted(Boolean.parseBoolean(questDataMap.get(field)));
            }
        }

        if (questDataMap.get(QuestField.DATE_COMPLETED.getField()) != null) {
            String redisDate = questDataMap.get(QuestField.DATE_COMPLETED.getField());
            Date retrievedDate = new Date(Long.parseLong(redisDate));
//            Bukkit.getLogger().warning("POPULATING?!?! quest CD in-memory for " + questId);
            cooldowns.put(Integer.parseInt(questId), retrievedDate);
        } else {
//            Bukkit.getLogger().log(Level.SEVERE, "no CD found in redis for " + questId);
        }
        return questWithUserData;
    }

    @SuppressWarnings("unchecked")
    @Override
    public QuestProfileData addDocumentToMongo() {
        MongoTemplate mongoTemplate = RunicCore.getDataAPI().getMongoTemplate();
        return mongoTemplate.save(this);
    }

    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, Object nestedObject, int... slot) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        String questId = (String) nestedObject;
        String parentKey = getJedisKey(uuid, slot[0]);
        return jedis.hgetAll(database + ":" + parentKey + ":" + questId); // get the parent key of the section
    }

    @Override
    public List<String> getFields() {
        return FIELDS;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public Map<String, String> toMap(Object nestedObject) {
        Quest quest = (Quest) nestedObject;
        return new HashMap<String, String>() {{
            put(QuestField.COMPLETED.getField(), String.valueOf(quest.getQuestState().isCompleted()));
            put(QuestField.STARTED.getField(), String.valueOf(quest.getQuestState().hasStarted()));
            put(QuestField.FIRST_NPC_STATE.getField(), quest.getFirstNPC().getState().getName());
            for (QuestObjective objective : quest.getObjectives()) {
                put("objectives:" + objective.getObjectiveNumber(), String.valueOf(objective.isCompleted()));
            }
            if (!QuestsUtil.canStartRepeatableQuest(uuid, quest)) {
                put("date-completed", String.valueOf(RunicQuests.getQuestCooldowns().get(uuid).get(quest.getQuestID()).getTime()));
            }
        }};
    }

    /**
     * Adds the object into session storage in redis
     *
     * @param jedis the jedis resource from core
     */
    @Override
    public void writeToJedis(Jedis jedis, int... slot) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        // Inform the server that this player should be saved to mongo on next task (jedis data is refreshed)
        jedis.sadd(database + ":" + "markedForSave:quests", this.uuid.toString());
        for (int characterSlot : this.questsMap.keySet()) {
            if (characterSlot == -1) continue; // Ensure we don't write data for error code slot
            // Ensure the system knows that there is data in redis
            jedis.sadd(database + ":" + this.uuid + ":questData", String.valueOf(characterSlot));
            jedis.expire(database + ":" + this.uuid + ":questData", RunicCore.getRedisAPI().getExpireTime());
            String key = getJedisKey(this.uuid, characterSlot);
            for (Quest quest : this.questsMap.get(characterSlot)) {
                if (quest == null) continue;
                // Skip quests without data
                if (!RunicQuests.getAPI().shouldWriteData(this.uuid, quest)) continue;
                jedis.del(database + ":" + key + ":" + quest.getQuestID()); // Reset map
                jedis.hmset(database + ":" + key + ":" + quest.getQuestID(), this.toMap(quest));
                jedis.expire(database + ":" + key + ":" + quest.getQuestID(), RunicCore.getRedisAPI().getExpireTime());
            }
        }
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public HashMap<Integer, HashMap<Integer, QuestDTO>> getQuestsDTOMap() {
        return questsDTOMap;
    }

    public void setQuestsDTOMap(HashMap<Integer, HashMap<Integer, QuestDTO>> questsDTOMap) {
        this.questsDTOMap = questsDTOMap;
    }

    public HashMap<Integer, List<Quest>> getQuestsMap() {
        return questsMap;
    }

    public void setQuestsMap(HashMap<Integer, List<Quest>> questsMap) {
        this.questsMap = questsMap;
    }

    /**
     * When we load an object from Mongo, this method converts the data objects into actual,
     * in-memory quests with data
     */
    public void loadQuestsFromDTOs() {
        for (int slot : this.questsDTOMap.keySet()) {
            this.questsMap.computeIfAbsent(slot, k -> new ArrayList<>());
            // Setup new quest data if it cannot be retrieved from mongo
            if (questsDTOMap.get(slot) == null) {
                this.questsMap.put(slot, QuestLoader.getQuestListNoUserData());
                return;
            }
            // Load quests from redis and populate DTO list for the given slot
            for (Quest questNoUserData : QuestLoader.getQuestListNoUserData()) {
                // Check if the quest is stored in mongo
                QuestDTO dtoFromMongo = questsDTOMap.get(slot).get(questNoUserData.getQuestID());
                if (dtoFromMongo != null) {
//                Bukkit.getLogger().log(Level.SEVERE, "writing DTO TO MEMORY MAP FROM MONGO");
                    Quest questWithUserData = writeUserDataToQuest(questNoUserData, dtoFromMongo);
                    questsMap.get(slot).add(questWithUserData);
                } else {
                    questsMap.get(slot).add(questNoUserData);
                }
            }
        }
        // Handle new character on previously existing data
        int slot = RunicCore.getCharacterAPI().getCharacterSlot(this.uuid);
        if (this.questsMap.get(slot) == null) {
            this.questsMap.put(slot, QuestLoader.getQuestListNoUserData());
        }
    }

    /**
     * When we load an object from redis (login and pre-shutdown), we prepare our DTO for
     * storage in mongo using this method
     *
     * @param jedis a jedis resource from the pool
     * @param slot  of the character to populate
     */
    public void populateDTOMapFromRedis(Jedis jedis, int slot) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        String parentKey = getJedisKey(uuid, slot);
        // Ensure the map for the character slot is not null
        this.questsDTOMap.computeIfAbsent(slot, k -> new HashMap<>());
        // Load quests from redis and populate DTO list for the given slot
        for (Quest questNoUserData : QuestLoader.getQuestListNoUserData()) {
            if (jedis.exists(database + ":" + parentKey + ":" + questNoUserData.getQuestID())) {
                Map<String, String> questDataMap = jedis.hgetAll(database + ":" + parentKey + ":" + questNoUserData.getQuestID()); // get the parent key of the section
                Quest questWithUserData = writeUserDataToQuest(uuid, questNoUserData, questDataMap, questNoUserData.getQuestID() + "");
                questsDTOMap.get(slot).put(questWithUserData.getQuestID(), new QuestDTO(questWithUserData));
                // Persist the cooldown for repeatable quests from Redis to Mongo
                if (questDataMap.get(QuestField.DATE_COMPLETED.getField()) != null) {
                    String redisDate = questDataMap.get(QuestField.DATE_COMPLETED.getField());
                    Date retrievedDate = new Date(Long.parseLong(redisDate));
//                    Bukkit.getLogger().warning("POPULATING DTO CD FOR " + questWithUserData.getQuestID());
                    questsDTOMap.get(slot).get(questWithUserData.getQuestID()).setCompletedDate(retrievedDate);
                }
            } else {
                questsDTOMap.get(slot).put(questNoUserData.getQuestID(), new QuestDTO(questNoUserData));
            }
        }
    }

    /**
     * Ensures that the DTO map matches the fresh data from Redis before save to Mongo
     */
    public void syncForMongo(Jedis jedis) {
        // Need to update our quests map as well with the DTO data from Spring (Mongo)
        for (int slot : this.questsMap.keySet()) {
            populateDTOMapFromRedis(jedis, slot);
        }
    }

    /**
     * When loading data from Mongo, writes persistent data to quests from the DTO
     * (Data Transfer Object)
     *
     * @param questNoUserData a blank quest without data
     * @param questDTO        the persistent fields from mongo
     * @return a Quest object with data written
     */
    private Quest writeUserDataToQuest(Quest questNoUserData, QuestDTO questDTO) {
        // Write base data from mongo DTO
        Quest questWithUserData = new Quest(questNoUserData);
        try {
            questWithUserData.getQuestState().setCompleted(questDTO.getState().isCompleted());
            questWithUserData.getFirstNPC().setState(questDTO.getFirstNpcState());
            questWithUserData.getQuestState().setStarted(questDTO.getState().hasStarted());
            // Write objectives
            for (QuestObjective questObjective : questNoUserData.getObjectives()) {
                QuestObjectiveDTO objectiveDTO = questDTO.getObjectivesMap().get(questObjective.getObjectiveNumber());
                if (objectiveDTO != null) { // Objective has some persistent data
                    questObjective.setCompleted(objectiveDTO.isCompleted());
                }
            }
            // Handle cooldowns for repeatable quests
            if (questDTO.getCompletedDate() != null) {
                RunicQuests.getQuestCooldowns().computeIfAbsent(uuid, k -> new HashMap<>());
                Map<Integer, Date> cooldowns = RunicQuests.getQuestCooldowns().get(uuid);
                cooldowns.put(questNoUserData.getQuestID(), questDTO.getCompletedDate());
            }
        } catch (Exception ex) {
            Bukkit.getLogger().warning("There was a problem writing quest data from Mongo!");
            ex.printStackTrace();
        }
        return questWithUserData;
    }


}
