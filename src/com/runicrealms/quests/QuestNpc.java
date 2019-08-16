package com.runicrealms.quests;

import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class QuestNpc {
	
	private NPC npc;
	private List<String> speech;
	private List<String> idleSpeech;
	private List<String> completedSpeech;
	
	public QuestNpc(Integer npcId, List<String> speech, List<String> idleSpeech, List<String> completedSpeech) {
		this.npc = CitizensAPI.getNPCRegistry().getById(npcId);
		this.speech = speech;
		this.idleSpeech = idleSpeech;
		this.completedSpeech = completedSpeech;
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

	public boolean hasIdleSpeech() {
		return this.idleSpeech != null;
	}
	
}
