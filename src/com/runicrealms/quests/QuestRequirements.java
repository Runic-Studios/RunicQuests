package com.runicrealms.quests;

import java.util.List;

public class QuestRequirements {
	
	public Integer levelReq;
	public Integer craftingReq;
	public CraftingProfessionType craftingType;
	public List<Integer> completedQuestsReq;
	public List<String> levelMsg;
	public List<String> craftingMsg;
	public List<String> completedQuestsMsg;
	
	public QuestRequirements(Integer levelReq, Integer craftingReq, CraftingProfessionType craftingType, List<Integer> completedQuestsReq, List<String> levelMsg, List<String> craftingMsg, List<String> completedQuestsMsg) {
		this.levelReq = levelReq;
		this.craftingReq = craftingReq;
		this.craftingType = craftingType;
		this.completedQuestsReq = completedQuestsReq;
		this.levelMsg = levelMsg;
		this.craftingMsg = craftingMsg;
		this.completedQuestsMsg = completedQuestsMsg;
	}
	
	public boolean hasCraftingRequirement() {
		return this.craftingReq != null;
	}
	
	public boolean hasCompletedQuestRequirement() {
		return this.completedQuestsReq != null;
	}
	
}
