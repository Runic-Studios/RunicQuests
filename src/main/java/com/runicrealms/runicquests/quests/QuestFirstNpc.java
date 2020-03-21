package com.runicrealms.runicquests.quests;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.runicrealms.runicquests.Plugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class QuestFirstNpc implements Cloneable {
	
	/*
	 * Represents a quest first NPC
	 */
	
	private NPC npc;
	private Integer npcId;
	private List<String> speech;
	private List<QuestIdleMessage> idleSpeech = null;
	private List<String> questCompletedSpeech;
	private String npcName;
	private List<String> execute;
	private FirstNpcState state = FirstNpcState.NEUTRAL;
	private Long id;
	
	public QuestFirstNpc(Integer npcId, List<String> speech, List<QuestIdleMessage> idleSpeech, List<String> questCompletedSpeech, String npcName, List<String> execute) {
		this.npc = CitizensAPI.getNPCRegistry().getById(npcId);
		this.npcId = npcId;
		this.speech = speech;
		this.idleSpeech = idleSpeech;
		this.questCompletedSpeech = questCompletedSpeech;
		this.execute = execute;
		this.npcName = npcName;
		this.id = Plugin.getNextId();
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
	
	public Long getId() {
		return this.id;
	}

	public List<QuestIdleMessage> getIdleSpeech() {
		return idleSpeech;
	}
	
	public boolean hasQuestCompletedSpeech() {
		return questCompletedSpeech != null;
	}

	public List<String> getQuestCompletedSpeech() {
		return questCompletedSpeech;
	}
	
	public String getNpcName() {
		return npcName;
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
			String parsedCommand = command.startsWith("/") ? command.substring(1).replaceAll("%player%", playerName) : command.replaceAll("%player%", playerName);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
		}
	}

	@Override
	public QuestFirstNpc clone() {
		return new QuestFirstNpc(this.npcId, this.speech, this.idleSpeech, this.questCompletedSpeech, this.npcName, this.execute);
	}

}
