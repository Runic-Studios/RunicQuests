package com.runicrealms.quests;

import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class QuestNpc {
	
	public NPC npc;
	public List<String> speech;
	public List<String> idleSpeech = null;
	public List<String> completedSpeech;
	
	public QuestNpc(Integer npcId, List<String> speech, List<String> idleSpeech, List<String> completedSpeech) {
		this.npc = CitizensAPI.getNPCRegistry().getById(npcId);
		this.speech = speech;
		this.idleSpeech = idleSpeech;
		this.completedSpeech = completedSpeech;
	}
	
	public boolean hasIdleSpeech() {
		return this.idleSpeech != null;
	}
	
}
