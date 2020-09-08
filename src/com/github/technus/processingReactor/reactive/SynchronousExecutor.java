package com.github.technus.processingReactor.reactive;

import java.util.Queue;
import java.util.concurrent.Executor;

public class SynchronousExecutor implements Executor {
    private final Queue<Runnable> queue;

    public SynchronousExecutor(String name, Queue<Runnable> queue) {
        Thread.currentThread().setName(name);
        this.queue = queue;
    }

    public synchronized void runSynchronousExecutor() {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            Runnable object = queue.poll();
            if (object == null) {
                break;
            }
            object.run();
        }
    }

    @Override
    public void execute(Runnable command) {
        queue.offer(command);
    }
}
