package com.runicrealms.quests;

import java.util.List;

import com.runicrealms.Plugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class QuestNpc {
	
	private NPC npc;
	private List<String> speech;
	private List<String> idleSpeech;
	private List<String> questCompletedSpeech;
	private String npcName;
	private SpeechState speechState = SpeechState.NOT_STARTED;
	private Integer id;
	
	public QuestNpc(Integer npcId, List<String> speech, List<String> idleSpeech, List<String> questCompletedSpeech, String npcName) {
		this.npc = CitizensAPI.getNPCRegistry().getById(npcId);
		this.speech = speech;
		this.idleSpeech = idleSpeech;
		this.questCompletedSpeech = questCompletedSpeech;
		this.npcName = npcName;
		this.id = Plugin.getNextId();
	}

	public NPC getCitizensNpc() {
		return npc;
	}

	public List<String> getSpeech() {
		return speech;
	}
	
	public Integer getId() {
		return this.id;
	}

	public List<String> getIdleSpeech() {
		return idleSpeech;
	}

	public List<String> getQuestCompletedSpeech() {
		return questCompletedSpeech;
	}
	
	public String getNpcName() {
		return this.npcName;
	}
	
	public SpeechState getSpeechState() {
		return speechState;
	}
	
	public void setSpeechState(SpeechState started) {
		speechState = started;
	}

	public boolean hasIdleSpeech() {
		return this.idleSpeech != null;
	}
	
}
