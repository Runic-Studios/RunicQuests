package com.runicrealms.quests;

import java.util.List;

import org.bukkit.Bukkit;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class QuestFirstNpc {
	
	private NPC npc;
	private List<String> speech;
	private List<String> idleSpeech = null;
	private List<String> completedSpeech;
	private List<String> execute;
	
	public QuestFirstNpc(Integer npcId, List<String> speech, List<String> idleSpeech, List<String> completedSpeech, List<String> execute) {
		this.npc = CitizensAPI.getNPCRegistry().getById(npcId);
		this.speech = speech;
		this.idleSpeech = idleSpeech;
		this.completedSpeech = completedSpeech;
		this.execute = execute;
	}
	
	public boolean hasIdleSpeech() {
		return this.idleSpeech != null;
	}
	
	public boolean hasExecute() {
		return execute != null;
	}
	
	public void executeCommand(String playerName) {
		for (String command : this.execute) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", playerName));
		}
	}

	public NPC getCitizensNpc() {
		return npc;
	}

	public List<String> getSpeech() {
		return speech;
	}

	public List<String> getIdleSpeech() {
		return idleSpeech;
	}

	public List<String> getCompletedSpeech() {
		return completedSpeech;
	}

}
