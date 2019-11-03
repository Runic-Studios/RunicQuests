package com.runicrealms.quests;

public enum FirstNpcState {

	/*
	 * Represents the first NPC status:
	 * Pending - quest is deniable, player has talked to NPC but has not accepted/denied quest yet
	 * Denied - the player has denied the quest
	 * neutral - the player has not yet talked to the NPC
	 * accepted - the player has accepted the quest and started it
	 */
	
	PENDING("pending"), DENIED("denied"), NEUTRAL("neutral"), ACCEPTED("accepted");
	
	private String name;
	
	private FirstNpcState(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public static FirstNpcState fromString(String str) {
		if (str.equalsIgnoreCase("pending")) {
			return FirstNpcState.PENDING;
		} else if (str.equalsIgnoreCase("denied")) {
			return FirstNpcState.DENIED;
		} else if (str.equalsIgnoreCase("neutral")) {
			return FirstNpcState.NEUTRAL;
		} else if (str.equalsIgnoreCase("accepted")) {
			return FirstNpcState.ACCEPTED;
		}
		return null;
	}
	
}
