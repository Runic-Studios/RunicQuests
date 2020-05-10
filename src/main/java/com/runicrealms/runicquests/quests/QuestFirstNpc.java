package com.runicrealms.runicquests.quests;

import java.util.List;

import com.runicrealms.runicquests.util.NpcPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.runicrealms.runicquests.Plugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class QuestFirstNpc implements Cloneable {
	
	/*
	 * Represents a quest first NPC
	 */
	
	private Integer npcId;
	private Location location;
	private List<String> speech;
	private List<QuestIdleMessage> idleSpeech = null;
	private List<String> questCompletedSpeech;
	private String npcName;
	private List<String> execute;
	private FirstNpcState state = FirstNpcState.NEUTRAL;
	private Long id;
	private NpcPlugin plugin;
	
	public QuestFirstNpc(Integer npcId, Location location, List<String> speech, List<QuestIdleMessage> idleSpeech, List<String> questCompletedSpeech, String npcName, List<String> execute, NpcPlugin plugin) {
		this.npcId = npcId;
		this.location = location;
		this.speech = speech;
		this.idleSpeech = idleSpeech;
		this.questCompletedSpeech = questCompletedSpeech;
		this.execute = execute;
		this.npcName = npcName;
		this.id = Plugin.getNextId();
		this.plugin = plugin;
	}

	public Integer getNpcId() {
		return this.npcId;
	}

	public Location getLocation() {
		return this.location;
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

	public NpcPlugin getPlugin() {
		return this.plugin;
	}
	
	public void executeCommand(String playerName) {
		for (String command : this.execute) {
			String parsedCommand = command.startsWith("/") ? command.substring(1).replaceAll("%player%", playerName) : command.replaceAll("%player%", playerName);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
		}
	}

	@Override
	public QuestFirstNpc clone() {
		return new QuestFirstNpc(this.npcId, this.location, this.speech, this.idleSpeech, this.questCompletedSpeech, this.npcName, this.execute, this.plugin);
	}

}
