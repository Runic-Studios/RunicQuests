package com.runicrealms.player;

public class QuestState {
	
	private boolean completed;
	private boolean started;
	
	public QuestState(boolean completed, boolean started) {
		this.setCompleted(completed);
		this.setStarted(started);
	}
	
	public QuestState(QuestState questState) {
		this.completed = questState.completed;
		this.started = questState.started;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean hasStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}
	
}
