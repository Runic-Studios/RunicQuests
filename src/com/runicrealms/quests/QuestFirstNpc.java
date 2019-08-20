package com.runicrealms.quests;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class QuestFirstNpc {
	
	private NPC npc;
	private List<String> speech;
	private List<String> idleSpeech = null;
	private List<String> questCompletedSpeech;
	private String npcName;
	private List<String> execute;
	private boolean deniable;
	private List<String> deniedMessage;
	private List<String> acceptedMessage;
	private FirstNpcState state = FirstNpcState.NEUTRAL;
	private SpeechState speechState = SpeechState.NOT_STARTED;
	
	public QuestFirstNpc(Integer npcId, List<String> speech, List<String> idleSpeech, List<String> questCompletedSpeech, String npcName, List<String> execute, boolean deniable, List<String> deniedMessage, List<String> acceptedMessage) {
		this.npc = CitizensAPI.getNPCRegistry().getById(npcId);
		this.speech = speech;
		this.idleSpeech = idleSpeech;
		this.questCompletedSpeech = questCompletedSpeech;
		this.execute = execute;
		this.npcName = npcName;
		this.deniable = deniable;
		this.deniedMessage = deniedMessage;
		this.acceptedMessage = acceptedMessage;
	}
	
	public NPC getCitizensNpc() {
		return npc;
	}

	public Location getStoredLocation() {
		return this.npc.getStoredLocation();
	}
	
	public List<String> getSpeech() {
		return speech;
	}

	public List<String> getIdleSpeech() {
		return idleSpeech;
	}

	public List<String> getQuestCompletedSpeech() {
		return questCompletedSpeech;
	}
	
	public String getNpcName() {
		return npcName;
	}
	
	public boolean isDeniable() {
		return deniable;
	}
	
	public List<String> getDeniedMessage() {
		return deniedMessage;
	}
	
	public List<String> getAcceptedMessage() {
		return acceptedMessage;
	}
	
	public SpeechState hasSpeechStarted() {
		return speechState;
	}
	
	public void setSpeechStarted(SpeechState state) {
		speechState = state;
	}
	
	public void setState(FirstNpcState state) {
		this.state = state;
	}
	
	public boolean hasIdleSpeech() {
		return this.idleSpeech != null;
	}
	
	public boolean hasExecute() {
		return execute != null;
	}
	
	public FirstNpcState getState() {
		return state;
	}
	
	public void executeCommand(String playerName) {
		for (String command : this.execute) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", playerName));
		}
	}

}
