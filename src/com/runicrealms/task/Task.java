package com.runicrealms.task;

import org.bukkit.scheduler.BukkitTask;

public class Task {

	private BukkitTask task;
	private Runnable runnable;
	
	public Task(BukkitTask task, Runnable runnable) {
		this.task = task;
		this.runnable = runnable;
	}
	
	public BukkitTask getTask() {
		return this.task;
	}
	
	public Runnable getRunnable() {
		return this.runnable;
	}
	
}
