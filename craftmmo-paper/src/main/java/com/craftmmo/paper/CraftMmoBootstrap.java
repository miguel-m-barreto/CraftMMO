package com.craftmmo.paper;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class CraftMmoBootstrap {
    public interface StartupAction {
        AutoCloseable start() throws Exception;
    }

    private final ExecutorService executor;
    private final StartupAction startupAction;
    private final AtomicReference<PluginLifecycleState> state = new AtomicReference<>(PluginLifecycleState.NEW);
    private final AtomicReference<AutoCloseable> runtime = new AtomicReference<>();
    private volatile CompletableFuture<Void> startupFuture = CompletableFuture.completedFuture(null);
    private volatile StartupFailure failure = StartupFailure.none();

    public CraftMmoBootstrap(ExecutorService executor, StartupAction startupAction) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.startupAction = Objects.requireNonNull(startupAction, "startupAction");
    }

    public void start() {
        if (!state.compareAndSet(PluginLifecycleState.NEW, PluginLifecycleState.STARTING)) {
            throw new IllegalStateException("Bootstrap can only be started from NEW");
        }
        startupFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return startupAction.start();
            } catch (Exception ex) {
                throw new StartupCompletionException(ex);
            }
        }, executor).handle((createdRuntime, throwable) -> {
            if (throwable != null) {
                failure = toFailure(unwrap(throwable));
                state.compareAndSet(PluginLifecycleState.STARTING, PluginLifecycleState.FAILED);
                closeQuietly(createdRuntime);
                return null;
            }
            if (!runtime.compareAndSet(null, createdRuntime)) {
                closeQuietly(createdRuntime);
                return null;
            }
            if (!state.compareAndSet(PluginLifecycleState.STARTING, PluginLifecycleState.READY)) {
                closeRuntime();
            }
            return null;
        });
    }

    public void stop(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout");
        PluginLifecycleState current;
        do {
            current = state.get();
            if (current == PluginLifecycleState.STOPPED || current == PluginLifecycleState.STOPPING) {
                return;
            }
        } while (!state.compareAndSet(current, PluginLifecycleState.STOPPING));

        closeRuntime();
        startupFuture.whenComplete((ignored, throwable) -> closeRuntime());
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        } finally {
            state.set(PluginLifecycleState.STOPPED);
        }
    }

    public PluginLifecycleState state() {
        return state.get();
    }

    public StartupFailure failure() {
        return failure;
    }

    private static StartupFailure toFailure(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && !(root instanceof CraftMmoStartupException)) {
            root = root.getCause();
        }
        StartupFailureCategory category = StartupFailureCategory.UNKNOWN;
        if (root instanceof CraftMmoStartupException startupException) {
            category = startupException.category();
        }
        String detail = root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
        return new StartupFailure(category, category.name(), sanitize(detail));
    }

    private static Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof java.util.concurrent.CompletionException
                || current instanceof StartupCompletionException) {
            if (current.getCause() == null) {
                break;
            }
            current = current.getCause();
        }
        return current;
    }

    private static String sanitize(String message) {
        return message
                .replaceAll("(?i)password=[^\\s;]+", "password=<redacted>")
                .replaceAll("(?i)user=[^\\s;]+", "user=<redacted>");
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
            // Shutdown paths cannot recover from close failures.
        }
    }

    private void closeRuntime() {
        closeQuietly(runtime.getAndSet(null));
    }

    private static final class StartupCompletionException extends RuntimeException {
        private StartupCompletionException(Throwable cause) {
            super(cause);
        }
    }
}
