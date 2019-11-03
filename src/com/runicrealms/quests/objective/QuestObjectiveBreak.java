package com.runicrealms.quests.objective;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;

import com.runicrealms.quests.QuestItem;
import com.runicrealms.quests.QuestObjectiveType;

public class QuestObjectiveBreak extends QuestObjective {
	
	/*
	 * Objective of type break
	 */
	
	private Material blockMaterial;
	private Integer blockAmount;
	private Location blockLocation;
	private Integer blocksBroken;
	
	public QuestObjectiveBreak(Material blockMaterial, Integer blockAmount, Location blockLocation, String goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		super(objectiveNumber, QuestObjectiveType.BREAK, completedMessage, goalMessage, execute);
		this.blockMaterial = blockMaterial;
		this.blockAmount = blockAmount;
		this.blockLocation = blockLocation;
	}
	
	public QuestObjectiveBreak(Material blockMaterial, Integer blockAmount, Location blockLocation, List<QuestItem> questItems, String goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage) {
		super(objectiveNumber, QuestObjectiveType.BREAK, completedMessage, questItems, goalMessage, execute);
		this.blockMaterial = blockMaterial;
		this.blockAmount = blockAmount;
		this.blockLocation = blockLocation;
	}
	
	public Material getBlockMaterial() {
		return this.blockMaterial;
	}
	
	public boolean hasBlockAmount() {
		return this.blockAmount != null;
	}
	
	public Integer getBlockAmount() {
		return this.blockAmount;
	}
	
	public boolean hasBlockLocation() {
		return this.blockLocation != null;
	}
	
	public Location getBlockLocation() {
		return this.blockLocation;
	}
	
	public void setBlocksBroken(Integer amount) {
		this.blocksBroken = amount;
	}
	
	public Integer getBlocksBroken() {
		return this.blocksBroken;
	}
	
}
