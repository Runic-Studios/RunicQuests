package com.runicrealms.plugin.runicquests.model;

import com.runicrealms.plugin.runicquests.quests.FirstNpcState;
import com.runicrealms.plugin.runicquests.quests.Quest;
import com.runicrealms.plugin.runicquests.quests.QuestState;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjective;

import java.util.Date;
import java.util.HashMap;

/**
 * This class is used to facilitate easy data storage in mongo. It only stores relevant fields
 */
public class QuestDTO {
    private FirstNpcState firstNpcState;
    private QuestState state;
    private HashMap<Integer, QuestObjectiveDTO> objectivesMap = new HashMap<>(); // Keyed by objective #
    private Date completedDate = null; // For repeatable quests

    @SuppressWarnings("unused")
    public QuestDTO() {
        // Default constructor for Spring
    }

    public QuestDTO(Quest quest) {
        this.firstNpcState = quest.getFirstNPC().getState();
        this.state = quest.getQuestState();
        for (QuestObjective objective : quest.getObjectives()) {
            objectivesMap.put(objective.getObjectiveNumber(), new QuestObjectiveDTO(objective));
        }
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    public FirstNpcState getFirstNpcState() {
        return firstNpcState;
    }

    public void setFirstNpcState(FirstNpcState firstNpcState) {
        this.firstNpcState = firstNpcState;
    }

    public HashMap<Integer, QuestObjectiveDTO> getObjectivesMap() {
        return objectivesMap;
    }

    public void setObjectivesMap(HashMap<Integer, QuestObjectiveDTO> objectivesMap) {
        this.objectivesMap = objectivesMap;
    }

    public QuestState getState() {
        return state;
    }

    public void setState(QuestState state) {
        this.state = state;
    }

}
