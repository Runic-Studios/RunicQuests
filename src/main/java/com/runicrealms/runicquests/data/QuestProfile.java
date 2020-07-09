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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class QuestProfile {

    private List<Quest> quests;
    private final String uuid;
    private final MongoData mongoData;
    private final Integer slot;
    private int questPoints;

    public QuestProfile(String uuid, Integer slot, Runnable onCompletion) {
        this.uuid = uuid;
        this.slot = slot;
        this.mongoData = new PlayerMongoData(uuid);
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
            boolean shouldSave = false;
            List<Quest> unusedQuests = QuestLoader.getUnusedQuestList();
            quests = new ArrayList<>();
            if (mongoData.has("character." + slot + ".quests")) {
                MongoDataSection questsData = mongoData.getSection("character." + slot + ".quests");
                for (Quest unusedQuest : unusedQuests) {
                    boolean hasQuestData = false;
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
                            hasQuestData = true;
                        }
                    }
                    if (!hasQuestData) {
                        quests.add(unusedQuest);
                        shouldSave = true;
                    }
                }
            } else {
                for (Quest quest : QuestLoader.getUnusedQuestList()) {
                    quests.add(quest);
                    shouldSave = true;
                }
            }
            Collections.sort(quests, new Comparator<Quest>() {
                @Override
                public int compare(Quest a, Quest b) {
                    if (a.getRequirements().getClassLvReq() > b.getRequirements().getClassLvReq()) {
                        return 1;
                    } else if (a.getRequirements().getClassLvReq() < b.getRequirements().getClassLvReq()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            if (!mongoData.has("character." + slot + ".quest-points")) {
                questPoints = 0;
                for (Quest quest : quests) {
                    if (quest.getQuestState().isCompleted()) {
                        questPoints++;
                    }
                }
                mongoData.set("character." + slot + ".quest-points", questPoints);
                mongoData.save();
            } else {
                questPoints = mongoData.get("character." + slot + ".quest-points", Integer.class);
            }
            onCompletion.run();
            if (shouldSave) {
                save(quests);
            }
        });
    }

    public List<Quest> getQuests() {
        return this.quests;
    }

    public void save(List<Quest> quests, int points) {
        this.quests = quests;
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
            MongoDataSection questsData = mongoData.getSection("character." + slot + ".quests");
            mongoData.set("character." + slot + ".quest-points", points);
            for (Quest quest : quests) {
                questsData.set(quest.getQuestID() + ".completed", quest.getQuestState().isCompleted());
                questsData.set(quest.getQuestID() + ".started", quest.getQuestState().hasStarted());
                questsData.set(quest.getQuestID() + ".first-npc-state", quest.getFirstNPC().getState().getName());
                for (QuestObjective objective : quest.getObjectives()) {
                    questsData.set(quest.getQuestID() + ".objectives." + objective.getObjectiveNumber(), objective.isCompleted());
                }
            }
            questsData.saveParent();
        });
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
                questsData.saveParent();
            }
        });
    }

    public int getQuestPoints() {
        return this.questPoints;
    }

    public void setQuestPoints(int amount) {
        this.questPoints = amount;
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
            mongoData.set("character." + slot + ".quest-points", amount);
            mongoData.save();
        });
    }

    public void save() {
        this.save(this.quests);
    }

    public void save(int points) {
        this.save(this.quests, points);
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
