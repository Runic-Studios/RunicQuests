package com.runicrealms.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.runicrealms.Plugin;

public class TaskQueue {
	
	private List<Runnable> tasks = new ArrayList<Runnable>();
	private HashMap<Runnable, Task> uncompletedTasks = new HashMap<Runnable, Task>();
	private double secsDelay = 3;
	private Runnable completedTask = null;
	
	public TaskQueue() {}
	
	public TaskQueue(Runnable... runnables) {
		for (Runnable runnable : runnables) {
			tasks.add(runnable);
		}
	}
	
	public TaskQueue(List<Runnable> runnables) {
		for (Runnable runnable : runnables) {
			tasks.add(runnable);
		}
	}
	
	public void nextTask() {
		for (Runnable runnable : uncompletedTasks.keySet()) {
			if (uncompletedTasks.get(runnable).getNumber() == 0) {
				runnable.run();
				if (uncompletedTasks.size() == 1 && completedTask != null) {
					completedTask.run();
				}
				uncompletedTasks.remove(runnable);
				continue;
			}
			uncompletedTasks.get(runnable).setNumber(uncompletedTasks.get(runnable).getNumber() - 1);
			uncompletedTasks.get(runnable).getTask().cancel();
		}
		HashMap<Runnable, Task> newTasks = new HashMap<Runnable, Task>();
		for (Runnable runnable : uncompletedTasks.keySet()) {
			newTasks.put(runnable, new Task(uncompletedTasks.get(runnable).getNumber(), Plugin.getInstance().getServer().getScheduler().runTaskLater(
					Plugin.getInstance(), 
					runnable, 
					(long) (this.secsDelay * (uncompletedTasks.get(runnable).getNumber() + 1) * 20))));
		}
		uncompletedTasks = newTasks;
	}
	
	public void startTasks() {
		for (int i = 0; i < this.tasks.size(); i++) {
			Runnable task = this.tasks.get(i);
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					task.run();
					if (uncompletedTasks.size() == 1 && completedTask != null) {
						completedTask.run();
					}
					uncompletedTasks.remove(this);
				}
			};
			uncompletedTasks.put(runnable, new Task(i, Plugin.getInstance().getServer().getScheduler().runTaskLater(
					Plugin.getInstance(), 
					runnable, 
					(long) (this.secsDelay * (i + 1) * 20))));
		}
	}
	
	public void addTasks(Runnable... runnables) {
		for (Runnable runnable : runnables) {
			tasks.add(runnable);
		}
	}
	
	public List<Runnable> getTasks() {
		return this.tasks;
	}
	
	public void setDelay(double delay) {
		this.secsDelay = delay;
	}
	
	public void setCompletedTask(Runnable task) {
		this.completedTask = task;
	}
	
}
