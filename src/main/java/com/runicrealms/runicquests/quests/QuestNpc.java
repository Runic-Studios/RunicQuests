package com.runicrealms.runicquests.quests;

import java.util.List;

import com.runicrealms.runicquests.Plugin;

import com.runicrealms.runicquests.util.NpcPlugin;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class QuestNpc implements Cloneable {
	
	/*
	 * Represents an objective's quest NPC
	 */

	private Integer npcId;
	private List<String> speech;
	private List<QuestIdleMessage> idleSpeech;
	private String npcName;
	private Long id;
	private NpcPlugin plugin;
	
	public QuestNpc(Integer npcId, List<String> speech, List<QuestIdleMessage> idleSpeech, String npcName, NpcPlugin plugin) {
		this.npcId = npcId;
		this.speech = speech;
		this.idleSpeech = idleSpeech;
		this.npcName = npcName;
		this.id = Plugin.getNextId();
		this.plugin = plugin;
	}

	public Integer getNpcId() {
		return this.npcId;
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
	
	public String getNpcName() {
		return this.npcName;
	}
	
	public boolean hasIdleSpeech() {
		return this.idleSpeech != null;
	}

	public NpcPlugin getPlugin() {
		return this.plugin;
	}

	@Override
	public QuestNpc clone() {
		return new QuestNpc(this.npcId, this.speech, this.idleSpeech, this.npcName, this.plugin);
	}

}
