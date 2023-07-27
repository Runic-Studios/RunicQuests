package com.runicrealms.plugin.runicquests.model;

import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.model.SessionDataMongo;
import com.runicrealms.plugin.runicquests.RunicQuests;
import com.runicrealms.plugin.runicquests.config.QuestLoader;
import com.runicrealms.plugin.runicquests.quests.Quest;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjective;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Document(collection = "quests")
@SuppressWarnings("unused")
public class QuestProfileData implements SessionDataMongo {
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
    private Map<Integer, Map<Integer, QuestDTO>> questsDTOMap = new HashMap<>();
    @Transient
    private Map<Integer, List<Quest>> questsMap = new HashMap<>(); // Keyed by character slot, not persisted to MongoDB

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
        this.questsMap = new HashMap<>() {{
            put(slot, QuestLoader.getQuestListNoUserData());
        }};
        RunicQuests.getQuestCooldowns().put(this.uuid, new HashMap<>());
    }

    /**
     * @return a map of DTOs with 'blank' data (all quests not started)
     */
    public static HashMap<Integer, QuestDTO> getBlankQuestDTOMap() {
        HashMap<Integer, QuestDTO> questDTOMap = new HashMap<>();
        for (Quest quest : QuestLoader.getQuestListNoUserData()) {
            questDTOMap.put(quest.getQuestID(), new QuestDTO(quest));
        }
        return questDTOMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public QuestProfileData addDocumentToMongo() {
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
        return mongoTemplate.save(this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Map<Integer, Map<Integer, QuestDTO>> getQuestsDTOMap() {
        return questsDTOMap;
    }

    public void setQuestsDTOMap(Map<Integer, Map<Integer, QuestDTO>> questsDTOMap) {
        this.questsDTOMap = questsDTOMap;
    }

    public Map<Integer, List<Quest>> getQuestsMap() {
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
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(this.uuid);
        if (this.questsMap.get(slot) == null) {
            this.questsMap.put(slot, QuestLoader.getQuestListNoUserData());
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
