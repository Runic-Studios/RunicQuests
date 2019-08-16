package com.runicrealms.quests;

import java.util.List;

public class QuestRequirements {
	
	private Integer levelReq;
	private Integer craftingReq;
	private CraftingProfessionType craftingType;
	private List<Integer> completedQuestsReq;
	private List<String> levelMsg;
	private List<String> craftingMsg;
	private List<String> completedQuestsMsg;
	
	public QuestRequirements(Integer levelReq, Integer craftingReq, CraftingProfessionType craftingType, List<Integer> completedQuestsReq, List<String> levelMsg, List<String> craftingMsg, List<String> completedQuestsMsg) {
		this.levelReq = levelReq;
		this.craftingReq = craftingReq;
		this.craftingType = craftingType;
		this.completedQuestsReq = completedQuestsReq;
		this.levelMsg = levelMsg;
		this.craftingMsg = craftingMsg;
		this.completedQuestsMsg = completedQuestsMsg;
	}

	public Integer getLevelRequirement() {
		return levelReq;
	}

	public Integer getCraftingRequirement() {
		return craftingReq;
	}

	public CraftingProfessionType getCraftingProfessionType() {
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
	
}
