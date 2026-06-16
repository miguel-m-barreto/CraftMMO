package com.craftmmo.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.craftmmo.api.identity.OperationId;
import com.craftmmo.api.identity.PayloadHash;
import com.craftmmo.api.profile.PlayerProfile;
import com.craftmmo.api.progression.PlayerSkillProgress;
import com.craftmmo.api.session.SessionLeaseResult;
import com.craftmmo.api.skill.SkillId;
import com.craftmmo.api.version.RulesetVersion;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class DomainValidationTest {
    @Test
    void validatesXpAndVersionInvariants() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerSkillProgress(SkillId.MINING, 0, 10, 9, 0));
        assertThrows(IllegalArgumentException.class, () -> new PlayerSkillProgress(SkillId.MINING, 0, 0, 0, -1));
        assertThrows(ArithmeticException.class, () -> new PlayerSkillProgress(SkillId.MINING, 0, 0, 0, Long.MAX_VALUE).nextVersion());
        assertThrows(IllegalArgumentException.class, () -> new PlayerProfile(
                com.craftmmo.api.identity.PlayerId.of(java.util.UUID.randomUUID()),
                "Player",
                new RulesetVersion("craftmmo-1.0.0"),
                Map.of(),
                Instant.parse("2026-06-16T00:00:00Z"),
                -1L,
                0L,
                0L,
                Instant.parse("2026-06-16T00:00:00Z"),
                Instant.parse("2026-06-16T00:00:00Z")
        ));
    }

    @Test
    void trimsIdentifiersAndRejectsInvalidHashesAndSessionStates() {
        assertEquals("op-1", new OperationId(" op-1 ").value());
        assertEquals("craftmmo-1.0.0", new RulesetVersion(" craftmmo-1.0.0 ").value());
        assertThrows(IllegalArgumentException.class, () -> new PayloadHash("bad"));
        assertThrows(IllegalArgumentException.class, () -> new SessionLeaseResult(com.craftmmo.api.session.SessionLeaseResultStatus.ACQUIRED, java.util.Optional.empty()));
    }
}
