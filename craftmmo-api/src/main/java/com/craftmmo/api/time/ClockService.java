package com.craftmmo.api.time;

import java.time.Instant;

public interface ClockService {
    Instant now();
}
