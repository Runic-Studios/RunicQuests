package com.runicrealms.runicquests.quests;

import java.util.List;

public class QuestRequirements {
	
	/*
	 * Contains a quest's requirements. Only level and level-not-met are required here
	 */
	
	private Integer levelReq;
	private Integer craftingReq;
	private List<CraftingProfessionType> craftingType;
	private List<Integer> completedQuestsReq;
	private List<String> levelMsg;
	private List<String> craftingMsg;
	private List<String> completedQuestsMsg;
	private PlayerClassType classType;
	private List<String> classTypeNotMet;
	
	public QuestRequirements(Integer levelReq, Integer craftingReq, List<CraftingProfessionType> craftingType, List<Integer> completedQuestsReq, List<String> levelMsg, List<String> craftingMsg, List<String> completedQuestsMsg, PlayerClassType classType, List<String> classTypeNotMet) {
		this.levelReq = levelReq;
		this.craftingReq = craftingReq;
		this.craftingType = craftingType;
		this.completedQuestsReq = completedQuestsReq;
		this.levelMsg = levelMsg;
		this.craftingMsg = craftingMsg;
		this.completedQuestsMsg = completedQuestsMsg;
		this.classType = classType;
		this.classTypeNotMet = classTypeNotMet;
	}

	public Integer getClassLvReq() {
		return levelReq;
	}

	public Integer getCraftingRequirement() {
		return craftingReq;
	}

	public List<CraftingProfessionType> getCraftingProfessionType() {
		return craftingType;
	}

	public List<Integer> getCompletedQuestsRequirement() {
		return completedQuestsReq;
	}

	public List<String> getLevelNotMetMsg() {
		return levelMsg;
	}

	public List<String> getCraftingLevelNotMetMsg() {
		return craftingMsg;
	}

	public List<String> getCompletedQuestsNotMetMsg() {
		return completedQuestsMsg;
	}

	public boolean hasCraftingRequirement() {
		return this.craftingReq != null;
	}
	
	public boolean hasCompletedQuestRequirement() {
		return this.completedQuestsReq != null;
	}
	
	public boolean hasClassTypeRequirement() {
		return this.classType != null;
	}
	
	public PlayerClassType getClassTypeRequirement() {
		return this.classType;
	}
	
	public List<String> getClassTypeNotMetMsg() {
		return this.classTypeNotMet;
	}

	public boolean hasLevelNotMetMsg() {
		return this.levelMsg != null;
	}

	public boolean hasCompletedQuestsNotMetMsg() {
		return this.completedQuestsMsg != null;
	}

	public boolean hasCraftingLevelNotMetMsg() {
		return this.craftingMsg != null;
	}

	public boolean hasClassNotMetMsg() {
		return this.classTypeNotMet != null;
	}
	
}
