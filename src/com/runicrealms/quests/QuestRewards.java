package com.runicrealms.quests;

import java.util.List;

import org.bukkit.Bukkit;

public class QuestRewards {

	private Integer exp;
	private Integer questPoints;
	private Integer money;
	private List<String> execute;
	
	public QuestRewards(Integer exp, Integer questPoints, Integer money, List<String> execute) {
		this.exp = exp;
		this.questPoints = questPoints;
		this.money = money;
		this.execute = execute;
	}
	
	public Integer getExperienceReward() {
		return this.exp;
	}
	
	public Integer getQuestPointsReward() {
		return this.questPoints;
	}
	
	public Integer getMoneyReward() {
		return this.money;
	}
	
	public boolean hasExecute() {
		return this.execute != null;
	}
	
	public void executeCommand(String playerName) {
		for (String command : this.execute) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", playerName));
		}
	}
}
