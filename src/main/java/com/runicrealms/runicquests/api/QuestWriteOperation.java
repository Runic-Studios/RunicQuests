package com.runicrealms.runicquests.api;

import com.runicrealms.plugin.rdb.api.WriteCallback;
import com.runicrealms.runicquests.model.QuestProfileData;

import java.util.UUID;

/**
 * Used for efficient updating of mongo document fields
 */
public interface QuestWriteOperation {

    /**
     * Updates a single field of the mapped 'QuestProfileData' document object
     *
     * @param uuid     of the player
     * @param slot     of the character
     * @param newValue the new value for the field
     * @param callback a function to execute on main thread when write operation is complete
     */
    void updateQuestProfileData(UUID uuid, int slot, QuestProfileData newValue, WriteCallback callback);

}
