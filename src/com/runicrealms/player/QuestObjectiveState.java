package com.runicrealms.player;

public class QuestObjectiveState {
	
	public boolean completed;
	public int speechState;
	
	public QuestObjectiveState() {
		this.completed = false;
		this.speechState = 0;
	}
	
	public QuestObjectiveState(boolean completed) {
		this.completed = completed;
		this.speechState = 0;
	}
	
}
