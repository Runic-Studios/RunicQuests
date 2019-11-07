package com.runicrealms.runicquests.quests;

import java.util.List;

import com.runicrealms.runicquests.Plugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class QuestNpc {
	
	/*
	 * Represents an objective's quest NPC
	 */
	
	private NPC npc;
	private List<String> speech;
	private List<QuestIdleMessage> idleSpeech;
	private String npcName;
	private Integer id;
	
	public QuestNpc(Integer npcId, List<String> speech, List<QuestIdleMessage> idleSpeech, String npcName) {
		this.npc = CitizensAPI.getNPCRegistry().getById(npcId);
		this.speech = speech;
		this.idleSpeech = idleSpeech;
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

	public List<QuestIdleMessage> getIdleSpeech() {
		return idleSpeech;
	}
	
	public String getNpcName() {
		return this.npcName;
	}
	
	public boolean hasIdleSpeech() {
		return this.idleSpeech != null;
	}
	
}
