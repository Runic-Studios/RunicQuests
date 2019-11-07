package com.runicrealms.runicquests.quests;

public enum QuestObjectiveType {

	/*
	 * Contains the different types of quest objectives
	 */
	
	SLAY("slay"), TALK("talk"), TRIPWIRE("tripwire"), BREAK("break");
	
	private String name;
	
	private QuestObjectiveType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
}
