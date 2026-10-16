package com.kuklin.telegrammvc.transport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ChatOrderedExecutorTest {

    private final ChatOrderedExecutor executor = new ChatOrderedExecutor(Executors.newFixedThreadPool(8));

    @AfterEach
    void shutdown() {
        executor.shutdown(Duration.ofSeconds(5));
    }

    @Test
    void runsTasksOfOneKeyInOrderAndNeverConcurrently() throws InterruptedException {
        int tasks = 500;
        List<Integer> order = new CopyOnWriteArrayList<>();
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maxActive = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(tasks);

        for (int i = 0; i < tasks; i++) {
            int n = i;
            executor.execute("chat", () -> {
                maxActive.accumulateAndGet(active.incrementAndGet(), Math::max);
                order.add(n);
                active.decrementAndGet();
                done.countDown();
            });
        }

        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(order).containsExactlyElementsOf(IntStream.range(0, tasks).boxed().toList());
        assertThat(maxActive.get()).isEqualTo(1);
    }

    @Test
    void runsDifferentKeysInParallel() throws InterruptedException {
        CountDownLatch bothStarted = new CountDownLatch(2);
        CountDownLatch done = new CountDownLatch(2);
        Runnable waitForOther = () -> {
            bothStarted.countDown();
            try {
                if (bothStarted.await(5, TimeUnit.SECONDS)) {
                    done.countDown();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        executor.execute("chat-1", waitForOther);
        executor.execute("chat-2", waitForOther);

        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void continuesAfterFailedTask() throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);

        executor.execute("chat", () -> {
            throw new IllegalStateException("handler failed");
        });
        executor.execute("chat", done::countDown);

        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void runsTasksWithoutKey() throws InterruptedException {
        CountDownLatch done = new CountDownLatch(3);

        for (int i = 0; i < 3; i++) {
            executor.execute(null, done::countDown);
        }

        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
    }
}
