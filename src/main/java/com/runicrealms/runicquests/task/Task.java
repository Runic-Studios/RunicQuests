package com.runicrealms.runicquests.task;

import org.bukkit.scheduler.BukkitTask;

/**
 * This class is meant to bind a bukkit task to a runnable.
 * The hasRun is to fix a minor bug where the task may run twice
 */
public class Task {
    private final BukkitTask task;
    private final Runnable runnable;
    private final boolean hasRun;

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
    } // fixes a bug

}
