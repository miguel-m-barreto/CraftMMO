package com.craftmmo.api.cooldown;

import com.craftmmo.api.identity.PlayerId;
import java.util.Objects;

public record CooldownKey(PlayerId playerId, String namespace, String key) {
    public CooldownKey {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(key, "key");
        namespace = namespace.trim();
        key = key.trim();
        if (namespace.isBlank() || key.isBlank()) {
            throw new IllegalArgumentException("Cooldown namespace and key must not be blank");
        }
    }
}
