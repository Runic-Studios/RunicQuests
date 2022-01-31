package com.runicrealms.runicquests.quests;

import com.runicrealms.runicnpcs.api.RunicNpcsAPI;
import com.runicrealms.runicquests.Plugin;
import org.bukkit.Location;

import java.util.List;

/**
 * Represents an objective's quest NPC
 */
public class QuestNpc implements Cloneable {

    private final Integer npcId;
    private final List<String> speech;
    private final boolean addNpcName;
    private final List<QuestIdleMessage> idleSpeech;
    private final String npcName;
    private final Long id;
    private final List<String> deniedMessage;

    public QuestNpc(Integer npcId, List<String> speech, boolean addNpcName, List<QuestIdleMessage> idleSpeech, String npcName, List<String> deniedMessage) {
        this.npcId = npcId;
        this.speech = speech;
        this.addNpcName = addNpcName;
        this.idleSpeech = idleSpeech;
        this.npcName = npcName;
        this.id = Plugin.getNextId();
        this.deniedMessage = deniedMessage;
    }

    /**
     * Static method to get the corresponding RunicNpc location for the given quest npc
     *
     * @param questNpc the questNpc wrapper object
     * @return a Location object
     */
    public static Location getQuestNpcLocation(QuestNpc questNpc) {
        return RunicNpcsAPI.getNpcById(questNpc.getNpcId()).getLocation();
    }

    public Integer getNpcId() {
        return this.npcId;
    }

    public List<String> getSpeech() {
        return speech;
    }

    public boolean addNpcName() {
        return this.addNpcName;
    }

    public Long getId() {
        return this.id;
    }

    public List<QuestIdleMessage> getIdleSpeech() {
        return idleSpeech;
    }

    public String getNpcName() {
        return this.npcName;
    }

    public boolean hasIdleSpeech() {
        return this.idleSpeech != null;
    }

    public boolean hasDeniedMessage() {
        return this.deniedMessage != null;
    }

    public List<String> getDeniedMessage() {
        return this.deniedMessage;
    }

    @Override
    public QuestNpc clone() {
        return new QuestNpc(this.npcId, this.speech, this.addNpcName, this.idleSpeech, this.npcName, this.deniedMessage);
    }

}
