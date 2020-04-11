package com.runicrealms.runicquests.quests;

import java.util.ArrayList;
import java.util.List;

public class QuestCooldowns {
	
	private String characterSlot;
	
	private List<Integer> cooldowns = new ArrayList<Integer>();
	
	public QuestCooldowns(String characterSlot) {
		this.characterSlot = characterSlot;
	}
	
	public String getCharacterSlot() {
		return this.characterSlot;
	}
	
	public List<Integer> getCooldowns() {
		return this.cooldowns;
	}
	
}
