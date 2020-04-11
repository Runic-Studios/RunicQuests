package com.runicrealms.runicquests.data;

import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.config.QuestLoader;
import com.runicrealms.runicquests.quests.FirstNpcState;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class QuestProfile {

    private List<Quest> quests;
    private String uuid;
    private MongoData mongoData;
    private Integer slot;

    public QuestProfile(String uuid, Integer slot) {
        this.uuid = uuid;
        this.slot = slot;
        this.mongoData = new PlayerMongoData(uuid);
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                List<Quest> unusedQuests = QuestLoader.getUnusedQuestList();
                quests = new ArrayList<Quest>();
                MongoDataSection questsData = mongoData.getSection("character." + slot + ".quests");
                for (Quest unusedQuest : unusedQuests) {
                    for (String dataQuestId : questsData.getKeys()) {
                        if (dataQuestId.equalsIgnoreCase(unusedQuest.getQuestID() + "")) {
                            MongoDataSection questData = questsData.getSection(dataQuestId);
                            Quest newQuest = new Quest(unusedQuest);
                            newQuest.getQuestState().setCompleted(questData.get("completed", Boolean.class));
                            newQuest.getQuestState().setStarted(questData.get("started", Boolean.class));
                            newQuest.getFirstNPC().setState(FirstNpcState.fromString(questData.get("first-npc-state", String.class)));
                            MongoDataSection objectivesData = questData.getSection("objectives");
                            for (String objectiveNumber : objectivesData.getKeys()) {
                                for (QuestObjective questObjective : unusedQuest.getObjectives()) {
                                    if (objectiveNumber.equalsIgnoreCase(questObjective.getObjectiveNumber() + "")) {
                                        questObjective.setCompleted(objectivesData.get(objectiveNumber + "", Boolean.class));
                                    }
                                }
                            }
                            quests.add(newQuest);
                        }
                    }
                }
                for (Quest unusedQuest : unusedQuests) {
                    boolean containsQuest = false;
                    for (Quest newQuest : quests) {
                        if (unusedQuest.getQuestID() == newQuest.getQuestID()) {
                            containsQuest = true;
                            break;
                        }
                    }
                    if (!containsQuest) {
                        quests.add(unusedQuest);
                    }
                }
            }
        });
    }

    public List<Quest> getQuests() {
        return this.quests;
    }

    public void save(List<Quest> quests) {
        this.quests = quests;
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                MongoDataSection questsData = mongoData.getSection("character." + slot + ".quests");
                for (Quest quest : quests) {
                    questsData.set(quest.getQuestID() + ".completed", quest.getQuestState().isCompleted());
                    questsData.set(quest.getQuestID() + ".started", quest.getQuestState().hasStarted());
                    questsData.set(quest.getQuestID() + ".first-npc-state", quest.getFirstNPC().getState().getName());
                    for (QuestObjective objective : quest.getObjectives()) {
                        questsData.set(quest.getQuestID() + ".objectives." + objective.getObjectiveNumber(), objective.isCompleted());
                    }
                }
            }
        });
    }

    public void save() {
        this.save(this.quests);
    }

    public String getUuid() {
        return this.uuid;
    }

    public Integer getSlot() {
        return this.slot;
    }

    public MongoData getMongoData() {
        return this.mongoData;
    }

}
