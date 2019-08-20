package com.runicrealms.quests;

public enum SpeechState {
	
	NOT_STARTED("not_started"), STARTED("started"), COMPLETED("completed");
	
	private String name;
	
	private SpeechState(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
}
