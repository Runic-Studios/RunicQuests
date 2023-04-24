package com.runicrealms.runicquests.config;

import org.bukkit.configuration.ConfigurationSection;

/**
 * A wrapper with useful information about a quest and a specific objective from a config section
 */
public class ConfigObjective {
    private final ConfigurationSection section;
    private final int questId;
    private final int numberOfObjectives;
    private final Integer objectiveNumber;

    /**
     * @param section            of the objective
     * @param questId            of the quest (unique)
     * @param numberOfObjectives is the total # of objectives
     * @param objectiveNumber    of the objective for this wrapper
     */
    public ConfigObjective(ConfigurationSection section, int questId, int numberOfObjectives, Integer objectiveNumber) {
        this.section = section;
        this.questId = questId;
        this.numberOfObjectives = numberOfObjectives;
        this.objectiveNumber = objectiveNumber;
    }

    public int getNumberOfObjectives() {
        return numberOfObjectives;
    }

    public Integer getObjectiveNumber() {
        return objectiveNumber;
    }

    public int getQuestId() {
        return questId;
    }

    public ConfigurationSection getSection() {
        return section;
    }
}
