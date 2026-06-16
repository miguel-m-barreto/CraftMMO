package com.craftmmo.testkit;

import com.craftmmo.api.random.RandomService;
import java.util.ArrayDeque;
import java.util.Queue;

public final class DeterministicRandomService implements RandomService {
    private final Queue<Double> doubles = new ArrayDeque<>();
    private final Queue<Integer> ints = new ArrayDeque<>();

    public DeterministicRandomService addDouble(double value) {
        doubles.add(value);
        return this;
    }

    public DeterministicRandomService addInt(int value) {
        ints.add(value);
        return this;
    }

    @Override
    public double nextDouble() {
        if (doubles.isEmpty()) {
            throw new IllegalStateException("No deterministic double value queued");
        }
        return doubles.remove();
    }

    @Override
    public int nextInt(int bound) {
        if (ints.isEmpty()) {
            throw new IllegalStateException("No deterministic int value queued");
        }
        int value = ints.remove();
        if (value < 0 || value >= bound) {
            throw new IllegalStateException("Queued deterministic int is outside requested bound");
        }
        return value;
    }
}
