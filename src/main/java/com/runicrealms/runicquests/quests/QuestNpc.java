package com.runicrealms.runicquests.quests;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.util.NpcPlugin;

import java.util.List;

public class QuestNpc implements Cloneable {
	
	/*
	 * Represents an objective's quest NPC
	 */

	private Integer npcId;
	private List<String> speech;
	private boolean addNpcName;
	private List<QuestIdleMessage> idleSpeech;
	private String npcName;
	private Long id;
	private NpcPlugin plugin;
	private List<String> deniedMessage;
	
	public QuestNpc(Integer npcId, List<String> speech, boolean addNpcName, List<QuestIdleMessage> idleSpeech, String npcName, NpcPlugin plugin, List<String> deniedMessage) {
		this.npcId = npcId;
		this.speech = speech;
		this.addNpcName = addNpcName;
		this.idleSpeech = idleSpeech;
		this.npcName = npcName;
		this.id = Plugin.getNextId();
		this.plugin = plugin;
		this.deniedMessage = deniedMessage;
	}

	public Integer getNpcId() {
		return this.npcId;
	}

	public List<String> getSpeech() {
		return speech;
	}

	public boolean addNpcName() {
		return this.addNpcName;
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

	public boolean hasDeniedMessage() {
		return this.deniedMessage != null;
	}

	public List<String> getDeniedMessage() {
		return this.deniedMessage;
	}

	@Override
	public QuestNpc clone() {
		return new QuestNpc(this.npcId, this.speech, this.addNpcName, this.idleSpeech, this.npcName, this.plugin, this.deniedMessage);
	}

}
