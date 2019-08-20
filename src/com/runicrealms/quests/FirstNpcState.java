package com.runicrealms.quests;

public enum FirstNpcState {

	PENDING("pending"), DENIED("denied"), NEUTRAL("neutral"), ACCEPTED("accepted");
	
	private String name;
	
	private FirstNpcState(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
}
