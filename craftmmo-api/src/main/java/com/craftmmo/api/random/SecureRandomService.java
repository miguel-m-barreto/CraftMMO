package com.craftmmo.api.random;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class SecureRandomService implements RandomService {
    private final RandomGenerator random;

    public SecureRandomService(RandomGenerator random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    public static SecureRandomService create() {
        return new SecureRandomService(new SecureRandom());
    }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
}
