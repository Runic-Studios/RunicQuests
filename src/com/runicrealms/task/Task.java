package com.runicrealms.task;

import org.bukkit.scheduler.BukkitTask;

public class Task {

	private int number;
	private BukkitTask task;
	
	public Task(int number, BukkitTask task) {
		this.number = number;
		this.task = task;
	}
	
	public int getNumber() {
		return this.number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public BukkitTask getTask() {
		return this.task;
	}
	
}
