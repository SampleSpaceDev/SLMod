package dev.samplespace.slmod.scheduler;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Scheduler {

    private static final Scheduler INSTANCE = new Scheduler();

    public static Scheduler get() {
        return INSTANCE;
    }

    private final List<Task> tasks;

    public Scheduler() {
        this.tasks = new ArrayList<>();
        ClientTickEvents.START_CLIENT_TICK.register(client -> this.runTasks());
    }

    public Task schedule(Runnable runnable) {
        Task task = new Task(runnable);
        this.tasks.add(task);
        return task;
    }

    public DelayedTask schedule(Runnable runnable, int delay) {
        DelayedTask task = new DelayedTask(runnable, delay);
        this.tasks.add(task);
        return task;
    }

    public RepeatingTask schedule(Runnable runnable, int initialDelay, int repeatInterval, int repeatCount) {
        RepeatingTask task = new RepeatingTask(runnable, initialDelay, repeatInterval, repeatCount);
        this.tasks.add(task);
        return task;
    }

    private void runTasks() {
        if (!MinecraftClient.getInstance().isOnThread()) {
            throw new UnsupportedOperationException("runTasks() can only be executed on the main thread");
        }

        Iterator<Task> iter = this.tasks.iterator();
        while (iter.hasNext()) {
            Task task = iter.next();
            if (task.isComplete) {
                iter.remove();
            } else {
                task.onTick();
                if (task.isComplete) {
                    iter.remove();
                }
            }
        }
    }

    public static class Task {

        private final Runnable runnable;
        protected boolean isComplete;

        public Task(Runnable runnable) {
            this.runnable = runnable;
            this.isComplete = false;
        }

        protected void onTick0() {
            this.runnable.run();
        }

        protected void onTick() {
            this.onTick0();
            this.isComplete = true;
        }

        public void cancel() {
            this.isComplete = true;
        }
    }

    public static class DelayedTask extends Task {

        private int delay;

        public DelayedTask(Runnable runnable, int delay) {
            super(runnable);
            this.delay = delay;
        }

        @Override
        protected void onTick() {
            if (this.delay > 0) {
                this.delay--;
                return;
            }
            super.onTick();
        }
    }

    public static class RepeatingTask extends Task {

        private int delay;
        private final int repeatInterval;
        private int repeatCount;

        public RepeatingTask(Runnable runnable, int initialDelay, int repeatInterval, int repeatCount) {
            super(runnable);
            this.delay = initialDelay;
            this.repeatInterval = repeatInterval;
            this.repeatCount = repeatCount;
        }

        @Override
        protected void onTick() {
            if (this.delay > 0) {
                this.delay--;
                return;
            }
            this.onTick0();
            if (this.repeatCount > 0) {
                this.repeatCount--;
                this.delay = this.repeatInterval - 1;
                return;
            }
            this.isComplete = true;
        }
    }
}
