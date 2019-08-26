package com.runicrealms.api;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.runicrealms.player.QuestProfile;
import com.runicrealms.quests.Quest;

public class QuestDenyEvent extends Event {

	private Quest quest;
	private QuestProfile profile;
	
	private static final HandlerList handlers = new HandlerList();
	
	public QuestDenyEvent(Quest quest, QuestProfile profile) {
		this.quest = quest;
		this.profile = profile;
	}
	
	public HandlerList getHandlers() {
	    return handlers;
	}

	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	public Quest getQuest() {
		return this.quest;
	}
	
	public QuestProfile getQuestProfile() {
		return this.profile;
	}
	
	public Player getPlayer() {
		try {
			return Bukkit.getPlayer(UUID.fromString(this.profile.getPlayerUUID()));
		} catch (Exception exception) {
			return null;
		}
	}
	
}