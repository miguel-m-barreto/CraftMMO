package com.craftmmo.api.time;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class SystemClockService implements ClockService {
    private final Clock clock;

    public SystemClockService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public static SystemClockService utc() {
        return new SystemClockService(Clock.systemUTC());
    }

    @Override
    public Instant now() {
        return clock.instant();
    }
}
