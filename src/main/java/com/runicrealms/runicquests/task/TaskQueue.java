package com.runicrealms.runicquests.task;

import com.runicrealms.runicquests.RunicQuests;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a queue of tasks, which can be run with an interval in between each task.
 * Use can use nextTask() to skip to the next task instantly.
 * This is used mainly for NPC text.
 * <p>
 * This code is very confusing, but should work as expected.
 */
public class TaskQueue {

    private final List<Runnable> tasks = new ArrayList<>(); // List of tasks
    private volatile ArrayList<Task> uncompletedTasks = new ArrayList<>(); // Tasks that are left to run
    private double secsDelay; // Interval between tasks
    private volatile Runnable completedTask = null; // An additional task that will be run with the last task in uncompletedTasks
    private volatile boolean canceled = false; // If this task queue has been canceled. This prevents a minor bug.

    public TaskQueue() {
        this.secsDelay = RunicQuests.NPC_MESSAGE_DELAY; // Get the config value for the interval
        /*
        TODO: this is a band-aid fix to remove autoscroll for holograms
         */
        if (this instanceof HologramTaskQueue)
            this.secsDelay = secsDelay * 30;
    }

    public TaskQueue(Runnable... runnables) { // Initialize the tasks
        tasks.addAll(Arrays.asList(runnables));
        this.secsDelay = RunicQuests.NPC_MESSAGE_DELAY;
    }

    public TaskQueue(List<Runnable> runnables) { // Initialize the tasks
        tasks.addAll(runnables);
        this.secsDelay = RunicQuests.NPC_MESSAGE_DELAY;
    }

    public void nextTask() { // Move instantly to the next task
        if (!canceled) { // Check that this has not been canceled
            if (!uncompletedTasks.get(0).hasRun()) { // Run the next tasks
                uncompletedTasks.get(0).getRunnable().run();
            }
            if (uncompletedTasks.size() == 0 && completedTask != null) { // Check that all the tasks have been run and there is a completed task
                try {
                    completedTask.run();
                    completedTask = null;
                    canceled = true;
                    uncompletedTasks.forEach(task -> task.getTask().cancel()); // Cancel all the current bukkit tasks left (bug fix)
                } catch (Exception ignored) {
                }
                return;
            }
            uncompletedTasks.forEach(task -> task.getTask().cancel()); // Cancel all the current bukkit tasks (we later re-schedule them)
            ArrayList<Task> newTasks = new ArrayList<>(); // Re-schedule all the tasks so they can be run at the correct time
            for (Task task : uncompletedTasks) { // Loop through the uncompleted tasks
                BukkitTask bukkitTask = RunicQuests.getInstance().getServer().getScheduler().runTaskLater( // Schedule each task with the correct interval
                        RunicQuests.getInstance(),
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
                } catch (Exception ignored) {
                }
                if (tasks.size() == 1 && completedTask != null) { // If this is the last task (there is only one task in the queue), and we have a completed task
                    try {
                        completedTask.run(); // Run the completed task
                        completedTask = null;
                        canceled = true;
                        uncompletedTasks.forEach(task_ -> task_.getTask().cancel()); // Cancel remaining tasks (bug fix)
                    } catch (Exception ignored) {
                    }
                    return;
                }
            } else { // If this is not the first task in the queue
                boolean lastTask = tasks.size() == i + 1; // Boolean that tells us if this is the last task
                Runnable runnable = new Runnable() { // Create new runnable
                    @Override
                    public void run() {
                        for (Task uncompletedTask : uncompletedTasks) { // Loop through the uncompleted tasks
                            if (uncompletedTask.getRunnable() == this) { // If the uncompleted runnable is this new one
                                if (!uncompletedTask.hasRun()) { // If the task has not been run
                                    task.run(); // Run the task
                                }
                                if (lastTask && completedTask != null) { // If this is the last task and we have a completed task
                                    try {
                                        completedTask.run(); // Run the completed task
                                        completedTask = null;
                                        canceled = true;
                                        uncompletedTasks.forEach(task_ -> task_.getTask().cancel()); // Cancel remaining tasks (bug fix)
                                        uncompletedTasks.remove(uncompletedTask);
                                    } catch (Exception ignored) {
                                    }
                                    return;
                                }
                                uncompletedTasks.remove(uncompletedTask); // Remove the uncompleted task that has been completed
                                break;
                            }
                        }
                    }
                };
                uncompletedTasks.add(new Task(
                        RunicQuests.getInstance().getServer().getScheduler().runTaskLater(RunicQuests.getInstance(), runnable, (long) (this.secsDelay * i * 20)), runnable, false)); // Schedule a new task
            }
        }
    }

    public void addTasks(Runnable... runnables) { // Add more tasks to the queue
        tasks.addAll(Arrays.asList(runnables));
    }

    /**
     * Cancel this particular queue
     */
    public void cancel() {
        canceled = true;
        uncompletedTasks.forEach(task -> task.getTask().cancel());
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
