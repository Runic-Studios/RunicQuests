package com.runicrealms.task;

import org.bukkit.scheduler.BukkitTask;

public class Task {

	private BukkitTask task;
	private Runnable runnable;
	private boolean hasRun;
	
	public Task(BukkitTask task, Runnable runnable, boolean hasRun) {
		this.task = task;
		this.runnable = runnable;
		this.hasRun = hasRun;
	}
	
	public BukkitTask getTask() {
		return this.task;
	}
	
	public Runnable getRunnable() {
		return this.runnable;
	}
	
	public boolean hasRun() {
		return this.hasRun;
	}
	
}
