package com.runicrealms.runicquests.quests;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.util.NpcPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.List;

public class QuestFirstNpc implements Cloneable {
	
	/*
	 * Represents a quest first NPC
	 */
	
	private Integer npcId;
	private Location location;
	private List<String> speech;
	private boolean addNpcName;
	private List<QuestIdleMessage> idleSpeech;
	private List<String> questCompletedSpeech;
	private String npcName;
	private List<String> execute;
	private FirstNpcState state = FirstNpcState.NEUTRAL;
	private Long id;
	private NpcPlugin plugin;
	private String goalMessage;
	private String goalLocation;
	
	public QuestFirstNpc(Integer npcId, Location location, List<String> speech, boolean addNpcName, List<QuestIdleMessage> idleSpeech, List<String> questCompletedSpeech, String npcName, List<String> execute, NpcPlugin plugin, String goalMessage, String goalLocation) {
		this.npcId = npcId;
		this.location = location;
		this.speech = speech;
		this.addNpcName = addNpcName;
		this.idleSpeech = idleSpeech;
		this.questCompletedSpeech = questCompletedSpeech;
		this.execute = execute;
		this.npcName = npcName;
		this.id = Plugin.getNextId();
		this.plugin = plugin;
		this.goalMessage = goalMessage;
		this.goalLocation = goalLocation;
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

	public boolean addNpcName() {
		return this.addNpcName;
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

	public boolean hasGoalMessage() {
		return this.goalMessage != null;
	}

	public String getGoalMessage() {
		return this.goalMessage;
	}

	public boolean hasGoalLocation() {
		return this.goalLocation != null;
	}

	public String getGoalLocation() {
		return this.goalLocation;
	}

	@Override
	public QuestFirstNpc clone() {
		return new QuestFirstNpc(this.npcId, this.location, this.speech, this.addNpcName, this.idleSpeech, this.questCompletedSpeech, this.npcName, this.execute, this.plugin, this.goalMessage, this.goalLocation);
	}

}
