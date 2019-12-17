package com.runicrealms.runicquests.task;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.scheduler.BukkitTask;

import com.runicrealms.runicquests.Plugin;

public class TaskQueue {
	
	/*
	 * Represents a queue of tasks, which can be run with an interval in between each task.
	 * Use can use nextTask() to skip to the next task instantly.
	 * This is used mainly for NPC text.
	 * 
	 * This code is EXTREMLY confusing, but should work as expected.
	 */

	private List<Runnable> tasks = new ArrayList<Runnable>(); // List of tasks
	private volatile ArrayList<Task> uncompletedTasks = new ArrayList<Task>(); // Tasks that are left to run
	private double secsDelay = 3; // Interval between tasks
	private volatile Runnable completedTask = null; // An additional task that will be run with the last task in uncompletedTasks
	private volatile boolean canceled = false; // If this task queue has been canceled. This prevents a minor bug.

	public TaskQueue() {
		this.secsDelay = Plugin.NPC_MESSAGE_DELAY; // Get the config value for the interval
	}

	public TaskQueue(Runnable... runnables) { // Initialize the tasks
		for (Runnable runnable : runnables) {
			tasks.add(runnable);
		}
		this.secsDelay = Plugin.NPC_MESSAGE_DELAY;
	}

	public TaskQueue(List<Runnable> runnables) { // Initialize the tasks
		for (Runnable runnable : runnables) {
			tasks.add(runnable);
		}
		this.secsDelay = Plugin.NPC_MESSAGE_DELAY;
	}

	public void nextTask() { // Move instantly to the next task
		if (!canceled) { // Check that this has not been canceled
			if (uncompletedTasks.get(0).hasRun() == false) { // Run the next tasks
				uncompletedTasks.get(0).getRunnable().run();
			}
			if (uncompletedTasks.size() == 0 && completedTask != null) { // Check that all the tasks have been run and there is a completed task
				try {
					completedTask.run();
					completedTask = null;
					canceled = true;
					uncompletedTasks.forEach(task -> task.getTask().cancel()); // Cancel all the current bukkit tasks left (bug fix)
				} catch (Exception exception) {}
				return;
			}
			uncompletedTasks.forEach(task -> task.getTask().cancel()); // Cancel all the current bukkit tasks (we later re-schedule them)
			ArrayList<Task> newTasks = new ArrayList<Task>(); // Re-schedule all the tasks so they can be run at the correct time
			for (Task task : uncompletedTasks) { // Loop through the uncompleted tasks
				BukkitTask bukkitTask = Plugin.getInstance().getServer().getScheduler().runTaskLater( // Schedule each task with the correct interval
						Plugin.getInstance(), 
						task.getRunnable(), 
						(long) (this.secsDelay * (uncompletedTasks.indexOf(task) + 1) * 20));
				newTasks.add(new Task(bukkitTask, task.getRunnable(), task.hasRun()));
			}
			uncompletedTasks = newTasks; // Change the uncompleted tasks to use the new variable
		}
	}

	public void startTasks() { // Starts the task queue
		for (int i = 0; i < this.tasks.size(); i++) { // Loop through the tasks
			Runnable task = this.tasks.get(i); // Get the runnable
			if (i == 0) { // Check if this is the first task
				try {
					task.run(); // Run the task
				} catch (Exception exception) {}
				if (tasks.size() == 1 && completedTask != null) { // If this is the last task (there is only one task in the queue), and we have a completed task
					try {
						completedTask.run(); // Run the completed task
						completedTask = null;
						canceled = true;
						uncompletedTasks.forEach(task_ -> task_.getTask().cancel()); // Cancel remaining tasks (bug fix)
					} catch (Exception exception) {}
					return;
				}
			} else { // If this is not the first task in the queue
				boolean lastTask = tasks.size() == i + 1; // Boolean that tells us if this is the last task
				Runnable runnable = new Runnable() { // Create new runnable
					@Override
					public void run() {
						for (Task uncompletedTask : uncompletedTasks) { // Loop through the uncompleted tasks
							if (uncompletedTask.getRunnable() == this) { // If the uncompleted runnable is this new one
								if (uncompletedTask.hasRun() == false) { // If the task has not been run
									task.run(); // Run the task
								}
								if (lastTask && completedTask != null) { // If this is the last task and we have a completed task
									try {
										completedTask.run(); // Run the completed task
										completedTask = null;
										canceled = true;
										uncompletedTasks.forEach(task_ -> task_.getTask().cancel()); // Cancel remaining tasks (bug fix)
										uncompletedTasks.remove(uncompletedTask);
									} catch (Exception exception) {}
									return;
								}
								uncompletedTasks.remove(uncompletedTask); // Remove the uncompleted task that has been completed
								break;
							}
						}
					}
				};
				uncompletedTasks.add(new Task(
						Plugin.getInstance().getServer().getScheduler().runTaskLater(Plugin.getInstance(), runnable, (long) (this.secsDelay * i * 20)), runnable, false)); // Schedule a new task
			}
		}
	}

	public void addTasks(Runnable... runnables) { // Add more tasks to the queue
		for (Runnable runnable : runnables) {
			tasks.add(runnable);
		}
	}

	public List<Runnable> getTasks() { // Get the runnables
		return this.tasks;
	}

	public void setDelay(double delay) { // Set the delay interval
		this.secsDelay = delay;
	}

	public void setCompletedTask(Runnable task) { // Set the completed task
		this.completedTask = task;
	}

}