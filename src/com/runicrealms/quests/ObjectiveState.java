package com.runicrealms.quests;

public class ObjectiveState {
	
	public boolean completed;
	public int speechState;
	
	public ObjectiveState() {
		this.completed = false;
		this.speechState = 0;
	}
	
	public ObjectiveState(boolean completed) {
		this.completed = completed;
		this.speechState = 0;
	}
	
}
