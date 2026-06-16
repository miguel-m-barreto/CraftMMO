package com.craftmmo.paper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

final class CraftMmoBootstrapTest {
    @Test
    void reachesReadyAfterSuccessfulStartup() throws Exception {
        AtomicBoolean closed = new AtomicBoolean(false);
        CraftMmoBootstrap bootstrap = new CraftMmoBootstrap(
                Executors.newSingleThreadExecutor(),
                () -> () -> closed.set(true)
        );

        bootstrap.start();
        awaitState(bootstrap, PluginLifecycleState.READY);
        bootstrap.stop(Duration.ofSeconds(2));

        assertEquals(PluginLifecycleState.STOPPED, bootstrap.state());
        assertTrue(closed.get());
    }

    @Test
    void recordsSanitizedFailure() throws Exception {
        CraftMmoBootstrap bootstrap = new CraftMmoBootstrap(
                Executors.newSingleThreadExecutor(),
                () -> {
                    throw new CraftMmoStartupException(
                            StartupFailureCategory.DATABASE,
                            "password=secret user=admin",
                            new IllegalStateException("password=secret user=admin")
                    );
                }
        );

        bootstrap.start();
        awaitState(bootstrap, PluginLifecycleState.FAILED);

        assertEquals(StartupFailureCategory.DATABASE, bootstrap.failure().category());
        assertEquals("DATABASE", bootstrap.failure().sanitizedMessage());
        assertTrue(bootstrap.failure().detail().contains("password=<redacted>"));
        bootstrap.stop(Duration.ofSeconds(2));
    }

    @Test
    void stopPreventsLateReady() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean closed = new AtomicBoolean(false);
        CraftMmoBootstrap bootstrap = new CraftMmoBootstrap(
                Executors.newSingleThreadExecutor(),
                () -> {
                    entered.countDown();
                    release.await(2, TimeUnit.SECONDS);
                    return () -> closed.set(true);
                }
        );

        bootstrap.start();
        assertTrue(entered.await(2, TimeUnit.SECONDS));
        bootstrap.stop(Duration.ofMillis(100));
        release.countDown();

        assertEquals(PluginLifecycleState.STOPPED, bootstrap.state());
    }

    @Test
    void repeatedConcurrentStopClosesRuntimeExactlyOnce() throws Exception {
        AtomicInteger closeCount = new AtomicInteger();
        CraftMmoBootstrap bootstrap = new CraftMmoBootstrap(
                Executors.newSingleThreadExecutor(),
                () -> closeCount::incrementAndGet
        );

        bootstrap.start();
        awaitState(bootstrap, PluginLifecycleState.READY);
        Thread firstStop = new Thread(() -> bootstrap.stop(Duration.ofSeconds(2)));
        Thread secondStop = new Thread(() -> bootstrap.stop(Duration.ofSeconds(2)));
        firstStop.start();
        secondStop.start();
        firstStop.join();
        secondStop.join();

        bootstrap.stop(Duration.ofSeconds(2));

        assertEquals(PluginLifecycleState.STOPPED, bootstrap.state());
        assertEquals(1, closeCount.get());
    }

    @Test
    void stopDuringStartupClosesLateRuntimeExactlyOnce() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger closeCount = new AtomicInteger();
        CraftMmoBootstrap bootstrap = new CraftMmoBootstrap(
                Executors.newSingleThreadExecutor(),
                () -> {
                    entered.countDown();
                    release.await(2, TimeUnit.SECONDS);
                    return closeCount::incrementAndGet;
                }
        );

        bootstrap.start();
        assertTrue(entered.await(2, TimeUnit.SECONDS));
        Thread stopper = new Thread(() -> bootstrap.stop(Duration.ofSeconds(2)));
        stopper.start();
        awaitState(bootstrap, PluginLifecycleState.STOPPING);
        release.countDown();
        stopper.join();

        assertEquals(PluginLifecycleState.STOPPED, bootstrap.state());
        assertEquals(1, closeCount.get());
    }

    private static void awaitState(CraftMmoBootstrap bootstrap, PluginLifecycleState expected) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            if (bootstrap.state() == expected) {
                return;
            }
            Thread.sleep(10);
        }
        assertEquals(expected, bootstrap.state());
    }
}
