package com.craftmmo.api.profile;

import com.craftmmo.api.identity.PlayerId;
import com.craftmmo.api.progression.PlayerSkillProgress;
import com.craftmmo.api.skill.SkillId;
import com.craftmmo.api.version.RulesetVersion;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record PlayerProfile(
        PlayerId playerId,
        String lastKnownName,
        RulesetVersion rulesetVersion,
        Map<SkillId, PlayerSkillProgress> progress,
        Instant lastLoginAt,
        long playtimeSeconds,
        long profileVersion,
        long lockVersion,
        Instant createdAt,
        Instant updatedAt
) {
    public PlayerProfile {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(lastKnownName, "lastKnownName");
        Objects.requireNonNull(rulesetVersion, "rulesetVersion");
        Objects.requireNonNull(progress, "progress");
        Objects.requireNonNull(lastLoginAt, "lastLoginAt");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        lastKnownName = lastKnownName.trim();
        progress = Map.copyOf(progress);
        if (lastKnownName.isBlank()) {
            throw new IllegalArgumentException("Last known name must not be blank");
        }
        if (playtimeSeconds < 0) {
            throw new IllegalArgumentException("Playtime seconds must not be negative");
        }
        if (profileVersion < 0 || lockVersion < 0) {
            throw new IllegalArgumentException("Versions must not be negative");
        }
    }

    public static PlayerProfile empty(PlayerId playerId, String lastKnownName, RulesetVersion rulesetVersion, Instant now) {
        return new PlayerProfile(playerId, lastKnownName, rulesetVersion, new EnumMap<>(SkillId.class), now, 0L, 0L, 0L, now, now);
    }

    public PlayerProfile withProfileUpdate(
            String newLastKnownName,
            RulesetVersion newRulesetVersion,
            Instant newLastLoginAt,
            long newPlaytimeSeconds,
            long newProfileVersion,
            Instant now
    ) {
        return new PlayerProfile(
                playerId,
                newLastKnownName,
                newRulesetVersion,
                progress,
                newLastLoginAt,
                newPlaytimeSeconds,
                newProfileVersion,
                Math.addExact(lockVersion, 1L),
                createdAt,
                now
        );
    }
}
