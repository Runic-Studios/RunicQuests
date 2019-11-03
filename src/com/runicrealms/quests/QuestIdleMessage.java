package com.runicrealms.quests;

import java.util.List;

public class QuestIdleMessage {
	
	/*
	 * Represents an NPC idle message, with its conditions
	 */
	
	private QuestIdleMessageConditions conditions;
	private List<String> speech;
	
	public QuestIdleMessage(QuestIdleMessageConditions conditions, List<String> speech) {
		this.conditions = conditions;
		this.speech = speech;
	}
	
	public QuestIdleMessageConditions getConditions() {
		return this.conditions;
	}
	
	public List<String> getSpeech() {
		return this.speech;
	}
	
}
