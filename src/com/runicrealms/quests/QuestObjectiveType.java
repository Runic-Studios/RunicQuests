package com.runicrealms.quests;

public enum QuestObjectiveType {

	SLAY("slay"), TALK("talk"), TRIPWIRE("tripwire"), BREAK("break");
	
	private String name;
	
	private QuestObjectiveType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
}
