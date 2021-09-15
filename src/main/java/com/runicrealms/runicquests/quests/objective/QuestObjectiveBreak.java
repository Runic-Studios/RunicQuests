package com.runicrealms.runicquests.quests.objective;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;

import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;

public class QuestObjectiveBreak extends QuestObjective {
	
	/*
	 * Objective of type break
	 */
	
	private Material blockMaterial;
	private Integer blockAmount;
	private Location blockLocation;
	private Integer blocksBroken;
	
	public QuestObjectiveBreak(Material blockMaterial, Integer blockAmount, Location blockLocation, List<QuestItem> questItems, String goalMessage, List<String> execute, Integer objectiveNumber, List<String> completedMessage, String goalLocation, boolean displayNextTitle) {
		super(objectiveNumber, QuestObjectiveType.BREAK, completedMessage, questItems, goalMessage, execute, goalLocation, displayNextTitle);
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

	@Override
	public QuestObjectiveBreak clone() {
		return new QuestObjectiveBreak(this.blockMaterial, this.blockAmount, this.blockLocation, this.questItems, this.goalMessage, this.execute, this.objectiveNumber, this.completedMessage, this.goalLocation, this.displayNextTitle);
	}
	
}
