package com.runicrealms.task;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import com.runicrealms.Plugin;

public class TaskQueue {

	private List<Runnable> tasks = new ArrayList<Runnable>();
	private volatile ArrayList<Task> uncompletedTasks = new ArrayList<Task>();
	private double secsDelay = 3;
	private volatile Runnable completedTask = null;
	private volatile boolean canceled = false;

	public TaskQueue() {
		this.secsDelay = Plugin.NPC_MESSAGE_DELAY;
	}

	public TaskQueue(Runnable... runnables) {
		for (Runnable runnable : runnables) {
			tasks.add(runnable);
		}
		this.secsDelay = Plugin.NPC_MESSAGE_DELAY;
	}

	public TaskQueue(List<Runnable> runnables) {
		for (Runnable runnable : runnables) {
			tasks.add(runnable);
		}
		this.secsDelay = Plugin.NPC_MESSAGE_DELAY;
	}

	public void nextTask() {
		if (!canceled) {
			if (uncompletedTasks.get(0).hasRun() == false) {
				uncompletedTasks.get(0).getRunnable().run();
			}
			if (uncompletedTasks.size() == 0 && completedTask != null) {
				try {
					completedTask.run();
					completedTask = null;
					canceled = true;
					uncompletedTasks.forEach(task -> task.getTask().cancel());
				} catch (Exception exception) {}
				return;
			}
			uncompletedTasks.forEach(task -> task.getTask().cancel());
			ArrayList<Task> newTasks = new ArrayList<Task>();
			for (Task task : uncompletedTasks) {
				BukkitTask bukkitTask = Plugin.getInstance().getServer().getScheduler().runTaskLater(
						Plugin.getInstance(), 
						task.getRunnable(), 
						(long) (this.secsDelay * (uncompletedTasks.indexOf(task) + 1) * 20));
				newTasks.add(new Task(bukkitTask, task.getRunnable(), task.hasRun()));
			}
			uncompletedTasks = newTasks;
		}
	}

	public void startTasks() {
		for (int i = 0; i < this.tasks.size(); i++) {
			Runnable task = this.tasks.get(i);
			if (i == 0) {
				try {
					task.run();
				} catch (Exception exception) {}
				if (tasks.size() == 1 && completedTask != null) {
					try {
						completedTask.run();
						completedTask = null;
						canceled = true;
						uncompletedTasks.forEach(task_ -> task_.getTask().cancel());
					} catch (Exception exception) {}
					return;
				}
			} else {
				boolean lastTask = tasks.size() == i + 1;
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						for (Task uncompletedTask : uncompletedTasks) {
							if (uncompletedTask.getRunnable() == this) {
								if (uncompletedTask.hasRun() == false) {
									task.run();
								}
								if (lastTask && completedTask != null) {
									try {
										completedTask.run();
										completedTask = null;
										canceled = true;
										uncompletedTasks.forEach(task_ -> task_.getTask().cancel());
										uncompletedTasks.remove(uncompletedTask);
									} catch (Exception exception) {}
									return;
								}
								uncompletedTasks.remove(uncompletedTask);
								break;
							}
						}
					}
				};
				uncompletedTasks.add(new Task(
						Plugin.getInstance().getServer().getScheduler().runTaskLater(Plugin.getInstance(), runnable, (long) (this.secsDelay * i * 20)), runnable, false));
			}
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
