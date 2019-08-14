package com.runicrealms.quests;

public class QuestState {
	
	public boolean completed;
	public boolean started;
	
	public QuestState(boolean completed, boolean started) {
		this.completed = completed;
		this.started = started;
	}
	
	public QuestState(QuestState questState) {
		this.completed = questState.completed;
		this.started = questState.started;
	}
	
}
