package com.runicrealms.quests;

import java.util.List;

import org.bukkit.Bukkit;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class QuestFirstNpc {
	
	public NPC npc;
	public List<String> speech;
	public List<String> idleSpeech = null;
	public List<String> completedSpeech;
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
	
}
