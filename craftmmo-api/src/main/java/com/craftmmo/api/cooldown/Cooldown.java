package com.craftmmo.api.cooldown;

import com.craftmmo.api.version.ContentVersion;
import com.craftmmo.api.version.RulesetVersion;
import java.time.Instant;
import java.util.Objects;

public record Cooldown(
        CooldownKey key,
        Instant startsAt,
        Instant endsAt,
        RulesetVersion rulesetVersion,
        ContentVersion contentVersion,
        long version
) {
    public Cooldown {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(startsAt, "startsAt");
        Objects.requireNonNull(endsAt, "endsAt");
        Objects.requireNonNull(rulesetVersion, "rulesetVersion");
        Objects.requireNonNull(contentVersion, "contentVersion");
        if (!endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("Cooldown end must be after start");
        }
        if (version < 0) {
            throw new IllegalArgumentException("Version must not be negative");
        }
    }

    public boolean activeAt(Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return !instant.isBefore(startsAt) && instant.isBefore(endsAt);
    }
}
