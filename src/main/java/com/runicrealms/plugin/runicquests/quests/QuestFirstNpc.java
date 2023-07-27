package com.runicrealms.plugin.runicquests.quests;

import com.runicrealms.plugin.runicquests.RunicQuests;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.List;

/**
 * Represents the first npc of a quest
 */
public class QuestFirstNpc implements Cloneable {
    private Integer npcId;
    private Location location;
    private List<String> speech;
    private boolean addNpcName;
    private List<QuestIdleMessage> idleSpeech;
    private List<String> questCompletedSpeech;
    private String npcName;
    private List<String> execute;
    private Long id;
    private String goalMessage;
    private String goalLocation;
    private FirstNpcState state = FirstNpcState.NEUTRAL;

    @SuppressWarnings("unused")
    public QuestFirstNpc() {
        // Default constructor for Spring
    }

    public QuestFirstNpc(Integer npcId, Location location, List<String> speech, boolean addNpcName,
                         List<QuestIdleMessage> idleSpeech, List<String> questCompletedSpeech, String npcName,
                         List<String> execute, String goalMessage, String goalLocation) {
        this.npcId = npcId;
        this.location = location;
        this.speech = speech;
        this.addNpcName = addNpcName;
        this.idleSpeech = idleSpeech;
        this.questCompletedSpeech = questCompletedSpeech;
        this.execute = execute;
        this.npcName = npcName;
        this.id = RunicQuests.getNextId();
        this.goalMessage = goalMessage;
        this.goalLocation = goalLocation;
    }

    public boolean addNpcName() {
        return this.addNpcName;
    }

    @Override
    public QuestFirstNpc clone() {
        return new QuestFirstNpc(this.npcId, this.location, this.speech, this.addNpcName, this.idleSpeech, this.questCompletedSpeech, this.npcName, this.execute, this.goalMessage, this.goalLocation);
    }

    public void executeCommand(String playerName) {
        for (String command : this.execute) {
            String parsedCommand = command.startsWith("/") ? command.substring(1).replaceAll("%player%", playerName) : command.replaceAll("%player%", playerName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
        }
    }

    public List<String> getExecute() {
        return execute;
    }

    public void setExecute(List<String> execute) {
        this.execute = execute;
    }

    public String getGoalLocation() {
        return this.goalLocation;
    }

    public void setGoalLocation(String goalLocation) {
        this.goalLocation = goalLocation;
    }

    public String getGoalMessage() {
        return this.goalMessage;
    }

    public void setGoalMessage(String goalMessage) {
        this.goalMessage = goalMessage;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<QuestIdleMessage> getIdleSpeech() {
        return idleSpeech;
    }

    public void setIdleSpeech(List<QuestIdleMessage> idleSpeech) {
        this.idleSpeech = idleSpeech;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getNpcId() {
        return this.npcId;
    }

    public void setNpcId(Integer npcId) {
        this.npcId = npcId;
    }

    public String getNpcName() {
        return npcName;
    }

    public void setNpcName(String npcName) {
        this.npcName = npcName;
    }

    public List<String> getQuestCompletedSpeech() {
        return questCompletedSpeech;
    }

    public void setQuestCompletedSpeech(List<String> questCompletedSpeech) {
        this.questCompletedSpeech = questCompletedSpeech;
    }

    public List<String> getSpeech() {
        return speech;
    }

    public void setSpeech(List<String> speech) {
        this.speech = speech;
    }

    public FirstNpcState getState() {
        return state;
    }

    public void setState(FirstNpcState state) {
        this.state = state;
    }

    public boolean hasExecute() {
        return execute != null;
    }

    public boolean hasGoalLocation() {
        return this.goalLocation != null;
    }

    public boolean hasGoalMessage() {
        return this.goalMessage != null;
    }

    public boolean hasIdleSpeech() {
        return this.idleSpeech != null;
    }

    public boolean hasQuestCompletedSpeech() {
        return questCompletedSpeech != null;
    }

    public boolean isAddNpcName() {
        return addNpcName;
    }

    public void setAddNpcName(boolean addNpcName) {
        this.addNpcName = addNpcName;
    }

}
