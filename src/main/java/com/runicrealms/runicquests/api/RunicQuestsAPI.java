package com.runicrealms.runicquests.api;

import com.runicrealms.runicquests.model.QuestProfileData;
import com.runicrealms.runicquests.quests.Quest;

import java.util.List;
import java.util.UUID;

public interface RunicQuestsAPI {

    /**
     * @return a list of all quests without player-specific data
     */
    List<Quest> getBlankQuestList();

    /**
     * Grabs the quest data wrapper for the given player
     *
     * @param uuid of the player
     * @return their quest data wrapper
     */
    QuestProfileData getQuestProfile(UUID uuid);

    /**
     * While QuestProfileData gives us all the data for a player, this method
     * gives us data specific to a character and their quest list
     *
     * @param profileData of the player-quest wrapper
     * @param slot        of the character
     * @return a list of quests (blank if there is no data)
     */
    List<Quest> loadQuestsList(QuestProfileData profileData, int slot);

    /**
     * Determines if the quest should save any persistent data.
     * For non-repeatable quests, just checks if quest has started or not.
     * Repeatable quests are a bit trickier. Checks if quest is started, completed, or on cooldown.
     * All are valid.
     *
     * @param uuid  of the player
     * @param quest the quest to check
     * @return true if it will be persisted to Redis/Mongo
     */
    boolean shouldWriteData(UUID uuid, Quest quest);

}
