package com.kuklin.telegrammvc.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Runs tasks on a shared pool, but tasks with the same key one after another, in submission order.
 * Different keys run in parallel. Each key has its own queue only while it has pending tasks.
 * <p>
 * After each task the next one of the same key is resubmitted to the pool instead of running in a loop,
 * so a busy chat cannot hold a thread while other chats wait.
 */
final class ChatOrderedExecutor {

    private static final Logger log = LoggerFactory.getLogger(ChatOrderedExecutor.class);

    private final ExecutorService pool;
    private final Map<Object, Queue<Runnable>> pending = new HashMap<>();

    ChatOrderedExecutor(ExecutorService pool) {
        this.pool = pool;
    }

    /**
     * @param key  tasks with equal keys never run concurrently; {@code null} means no ordering
     * @param task task to run
     */
    void execute(Object key, Runnable task) {
        if (key == null) {
            submit(() -> runSafely(task));
            return;
        }
        synchronized (pending) {
            Queue<Runnable> queue = pending.get(key);
            if (queue != null) {
                queue.add(task);
                return;
            }
            pending.put(key, new ArrayDeque<>());
        }
        submit(() -> runAndContinue(key, task));
    }

    private void runAndContinue(Object key, Runnable task) {
        runSafely(task);
        Runnable next;
        synchronized (pending) {
            next = pending.get(key).poll();
            if (next == null) {
                pending.remove(key);
            }
        }
        if (next != null) {
            Runnable nextTask = next;
            submit(() -> runAndContinue(key, nextTask));
        }
    }

    private void runSafely(Runnable task) {
        try {
            task.run();
        } catch (Throwable e) {
            log.error("Update handling failed", e);
        }
    }

    private void submit(Runnable task) {
        try {
            pool.execute(task);
        } catch (RejectedExecutionException e) {
            log.warn("Update dropped: handling pool is shut down");
        }
    }

    /**
     * Stops accepting tasks and waits for running ones.
     */
    void shutdown(Duration timeout) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                log.warn("Update handlers did not finish in {}, interrupting", timeout);
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
