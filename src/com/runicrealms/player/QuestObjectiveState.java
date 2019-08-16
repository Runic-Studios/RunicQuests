package com.runicrealms.player;

public class QuestObjectiveState {
	
	private boolean completed;
	private int speechState;
	
	public QuestObjectiveState() {
		this.setCompleted(false);
		this.setSpeechState(0);
	}
	
	public QuestObjectiveState(boolean completed) {
		this.setCompleted(completed);
		this.setSpeechState(0);
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public int getSpeechState() {
		return speechState;
	}

	public void setSpeechState(int speechState) {
		this.speechState = speechState;
	}
	
	
	
}
