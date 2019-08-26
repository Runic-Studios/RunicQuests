package com.runicrealms.task;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.scheduler.BukkitTask;

import com.runicrealms.Plugin;

public class TaskQueue {

	private List<Runnable> tasks = new ArrayList<Runnable>();
	private ArrayList<Task> uncompletedTasks = new ArrayList<Task>();
	private double secsDelay = 3;
	private Runnable completedTask = null;

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
		uncompletedTasks.get(0).getRunnable().run();
		if (uncompletedTasks.size() == 0 && completedTask != null) {
			try {
				completedTask.run();
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
			newTasks.add(new Task(bukkitTask, task.getRunnable()));
		}
		uncompletedTasks = newTasks;
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
								task.run();
								if (lastTask && completedTask != null) {
									try {
										completedTask.run();
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
						Plugin.getInstance().getServer().getScheduler().runTaskLater(Plugin.getInstance(), runnable, (long) (this.secsDelay * i * 20)), 
						runnable));
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
