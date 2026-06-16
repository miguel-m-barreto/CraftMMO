package com.craftmmo.testkit;

import com.craftmmo.api.time.ClockService;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class DeterministicClockService implements ClockService {
    private final AtomicReference<Instant> now;

    public DeterministicClockService(Instant initial) {
        this.now = new AtomicReference<>(Objects.requireNonNull(initial, "initial"));
    }

    @Override
    public Instant now() {
        return now.get();
    }

    public void advance(Duration duration) {
        now.updateAndGet(current -> current.plus(duration));
    }

    public void set(Instant instant) {
        now.set(Objects.requireNonNull(instant, "instant"));
    }
}
