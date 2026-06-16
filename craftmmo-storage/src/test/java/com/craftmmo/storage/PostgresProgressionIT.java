package com.craftmmo.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.craftmmo.api.cooldown.Cooldown;
import com.craftmmo.api.cooldown.CooldownKey;
import com.craftmmo.api.identity.OperationId;
import com.craftmmo.api.identity.PlayerId;
import com.craftmmo.api.profile.PlayerProfile;
import com.craftmmo.api.progression.PlayerSkillProgress;
import com.craftmmo.api.session.SessionLeaseResult;
import com.craftmmo.api.skill.SkillId;
import com.craftmmo.api.time.SystemClockService;
import com.craftmmo.api.version.ContentVersion;
import com.craftmmo.api.version.RulesetVersion;
import com.craftmmo.core.progression.NoFormulaLevelingPolicy;
import com.craftmmo.core.progression.ProgressionApplyStatus;
import com.craftmmo.core.progression.SkillProgressionRequest;
import com.craftmmo.core.progression.SkillProgressionService;
import com.craftmmo.storage.cooldown.JdbcCooldownRepository;
import com.craftmmo.storage.jdbc.JdbcConnectionContext;
import com.craftmmo.storage.jdbc.JdbcStorageException;
import com.craftmmo.storage.jdbc.JdbcTransactionRunner;
import com.craftmmo.storage.migration.FlywayMigrator;
import com.craftmmo.storage.player.JdbcPlayerRepository;
import com.craftmmo.storage.progression.JdbcProgressionRepository;
import com.craftmmo.storage.session.JdbcSessionLeaseRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.postgresql.ds.PGSimpleDataSource;

@Testcontainers
final class PostgresProgressionIT {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("craftmmo")
            .withUsername("craftmmo")
            .withPassword("craftmmo");

    static DataSource dataSource;

    @BeforeAll
    static void migrate() {
        PGSimpleDataSource pg = new PGSimpleDataSource();
        pg.setUrl(POSTGRES.getJdbcUrl());
        pg.setUser(POSTGRES.getUsername());
        pg.setPassword(POSTGRES.getPassword());
        dataSource = pg;
        new FlywayMigrator().migrate(dataSource);
    }

    @Test
    void appliesProgressionOncePerOperationId() throws Exception {
        PlayerId playerId = createPlayer("Player");
        Instant now = Instant.parse("2026-06-16T00:00:00Z");
        JdbcConnectionContext context = new JdbcConnectionContext();
        SkillProgressionService service = new SkillProgressionService(
                new JdbcProgressionRepository(context),
                new JdbcTransactionRunner(dataSource, context),
                new NoFormulaLevelingPolicy(),
                new SystemClockService(Clock.fixed(now, ZoneOffset.UTC))
        );
        OperationId operationId = new OperationId("test-operation");
        SkillProgressionRequest request = SkillProgressionRequest.xpGrant(operationId, playerId, SkillId.MINING, "test", 25);

        assertEquals(ProgressionApplyStatus.APPLIED, service.applyXp(request).status());
        assertEquals(ProgressionApplyStatus.DUPLICATE_REPLAY, service.applyXp(request).status());
        assertEquals(
                ProgressionApplyStatus.IDENTITY_CONFLICT,
                service.applyXp(SkillProgressionRequest.xpGrant(operationId, playerId, SkillId.MINING, "test", 30)).status()
        );

        JdbcTransactionRunner runner = new JdbcTransactionRunner(dataSource, context);
        JdbcProgressionRepository repository = new JdbcProgressionRepository(context);
        assertThrows(JdbcStorageException.class, () -> runner.inTransaction(() -> {
            repository.markOperationApplied(operationId, new PlayerSkillProgress(SkillId.MINING, 0, 25, 25, 1), now);
            return null;
        }));
    }

    @Test
    void forgedProgressionOperationWithDifferentPersistedXpIsIdentityConflict() throws Exception {
        PlayerId playerId = createPlayer("Forged");
        Instant now = Instant.parse("2026-06-16T00:00:00Z");
        OperationId operationId = new OperationId("forged-operation");
        SkillProgressionRequest request = SkillProgressionRequest.xpGrant(operationId, playerId, SkillId.MINING, "test", 25);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     insert into progression_operations (
                         operation_id, player_uuid, skill_id, operation_type, source, payload_hash, xp, status, created_at, updated_at
                     )
                     values (?, ?, ?, ?, ?, ?, ?, 'STARTED', ?, ?)
                     """)) {
            statement.setString(1, operationId.value());
            statement.setObject(2, playerId.value());
            statement.setString(3, SkillId.MINING.stableId());
            statement.setString(4, request.operationType().name());
            statement.setString(5, request.source());
            statement.setString(6, request.payloadHash().value());
            statement.setLong(7, 99L);
            statement.setTimestamp(8, Timestamp.from(now));
            statement.setTimestamp(9, Timestamp.from(now));
            statement.executeUpdate();
        }

        JdbcConnectionContext context = new JdbcConnectionContext();
        SkillProgressionService service = new SkillProgressionService(
                new JdbcProgressionRepository(context),
                new JdbcTransactionRunner(dataSource, context),
                new NoFormulaLevelingPolicy(),
                new SystemClockService(Clock.fixed(now, ZoneOffset.UTC))
        );

        assertEquals(ProgressionApplyStatus.IDENTITY_CONFLICT, service.applyXp(request).status());
    }

    @Test
    void rollsBackTransactionAndDetectsOptimisticLock() throws Exception {
        PlayerId playerId = createPlayer("Rollback");
        JdbcConnectionContext context = new JdbcConnectionContext();
        JdbcTransactionRunner runner = new JdbcTransactionRunner(dataSource, context);
        JdbcProgressionRepository repository = new JdbcProgressionRepository(context);

        assertThrows(IllegalStateException.class, () -> runner.inTransaction(() -> {
            repository.save(playerId, PlayerSkillProgress.initial(SkillId.MINING), new PlayerSkillProgress(SkillId.MINING, 0, 1, 1, 1), Instant.now());
            throw new IllegalStateException("rollback");
        }));

        runner.inTransaction(() -> {
            assertTrue(repository.findForUpdate(playerId, SkillId.MINING).isEmpty());
            PlayerSkillProgress previous = new PlayerSkillProgress(SkillId.MINING, 0, 0, 0, 99);
            assertFalse(repository.save(playerId, previous, new PlayerSkillProgress(SkillId.MINING, 0, 1, 1, 100), Instant.now()));
            return null;
        });
    }

    @Test
    void persistsPlayerCooldownAndSessionLeaseLifecycle() {
        PlayerId playerId = PlayerId.of(UUID.randomUUID());
        Instant now = Instant.parse("2026-06-16T00:00:00Z");
        JdbcConnectionContext context = new JdbcConnectionContext();
        JdbcTransactionRunner runner = new JdbcTransactionRunner(dataSource, context);
        JdbcPlayerRepository players = new JdbcPlayerRepository(context);
        JdbcCooldownRepository cooldowns = new JdbcCooldownRepository(context);
        JdbcSessionLeaseRepository leases = new JdbcSessionLeaseRepository(context);
        UUID leaseId = UUID.randomUUID();

        runner.inTransaction(() -> {
            PlayerProfile created = PlayerProfile.empty(playerId, "Player", new RulesetVersion("craftmmo-1.0.0"), now);
            assertTrue(players.insert(created));
            PlayerProfile loaded = players.find(playerId).orElseThrow();
            PlayerProfile updated = loaded.withProfileUpdate(
                    "PlayerTwo",
                    new RulesetVersion("craftmmo-1.0.1"),
                    now.plusSeconds(30),
                    30L,
                    1L,
                    now.plusSeconds(30)
            );
            assertTrue(players.update(loaded, updated));
            assertFalse(players.update(loaded, updated));
            PlayerProfile reloaded = players.find(playerId).orElseThrow();
            assertEquals("PlayerTwo", reloaded.lastKnownName());
            assertEquals(30L, reloaded.playtimeSeconds());
            assertEquals(1L, reloaded.profileVersion());
            assertEquals(1L, reloaded.lockVersion());
            PlayerProfile staleNext = loaded.withProfileUpdate(
                    "Stale",
                    loaded.rulesetVersion(),
                    loaded.lastLoginAt(),
                    loaded.playtimeSeconds(),
                    Math.addExact(loaded.profileVersion(), 1L),
                    now.plusSeconds(40)
            );
            assertFalse(players.update(loaded, staleNext));
            assertThrows(IllegalArgumentException.class, () -> players.update(reloaded, new PlayerProfile(
                    reloaded.playerId(),
                    reloaded.lastKnownName(),
                    reloaded.rulesetVersion(),
                    reloaded.progress(),
                    reloaded.lastLoginAt(),
                    reloaded.playtimeSeconds(),
                    reloaded.profileVersion(),
                    Math.addExact(reloaded.lockVersion(), 1L),
                    reloaded.createdAt(),
                    now.plusSeconds(50)
            )));
            assertThrows(IllegalArgumentException.class, () -> players.update(reloaded, new PlayerProfile(
                    reloaded.playerId(),
                    reloaded.lastKnownName(),
                    reloaded.rulesetVersion(),
                    reloaded.progress(),
                    reloaded.lastLoginAt(),
                    reloaded.playtimeSeconds(),
                    Math.addExact(reloaded.profileVersion(), 2L),
                    Math.addExact(reloaded.lockVersion(), 2L),
                    reloaded.createdAt(),
                    now.plusSeconds(50)
            )));

            CooldownKey cooldownKey = new CooldownKey(playerId, "core", "test");
            Cooldown initialCooldown = new Cooldown(
                    cooldownKey,
                    now,
                    now.plusSeconds(60),
                    new RulesetVersion("craftmmo-1.0.0"),
                    new ContentVersion("content-1"),
                    0L
            );
            Cooldown nextCooldown = new Cooldown(
                    cooldownKey,
                    now,
                    now.plusSeconds(120),
                    new RulesetVersion("craftmmo-1.0.0"),
                    new ContentVersion("content-1"),
                    1L
            );
            assertTrue(cooldowns.save(initialCooldown, nextCooldown, now));
            assertTrue(cooldowns.find(cooldownKey).orElseThrow().activeAt(now.plusSeconds(30)));

            SessionLeaseResult acquired = leases.acquire(playerId, "server-a", leaseId, now, now.plus(Duration.ofMinutes(1)));
            assertTrue(acquired.acquired());
            assertThrows(IllegalArgumentException.class, () -> leases.acquire(playerId, "server-c", UUID.randomUUID(), now, now));
            assertThrows(IllegalArgumentException.class, () -> leases.renew(playerId, leaseId, now, now));
            assertFalse(leases.acquire(playerId, "server-b", UUID.randomUUID(), now, now.plus(Duration.ofMinutes(1))).acquired());
            assertTrue(leases.renew(playerId, leaseId, now.plusSeconds(10), now.plus(Duration.ofMinutes(2))).isPresent());
            assertTrue(leases.acquire(playerId, "server-b", UUID.randomUUID(), now.plus(Duration.ofMinutes(3)), now.plus(Duration.ofMinutes(4))).acquired());
            assertFalse(leases.release(playerId, leaseId));
            return null;
        });
    }

    @Test
    void upsertWithStaleVersionFieldsUsesExistingVersions() throws Exception {
        PlayerId playerId = PlayerId.of(UUID.randomUUID());
        Instant now = Instant.parse("2026-06-16T00:00:00Z");
        JdbcConnectionContext context = new JdbcConnectionContext();
        JdbcTransactionRunner runner = new JdbcTransactionRunner(dataSource, context);
        JdbcPlayerRepository players = new JdbcPlayerRepository(context);

        // Insert the player so profileVersion=0 and lockVersion=0 are persisted.
        runner.inTransaction(() -> {
            players.insert(PlayerProfile.empty(playerId, "Initial", new RulesetVersion("craftmmo-1.0.0"), now));
            return null;
        });

        // Build an incoming profile with stale version fields (profileVersion=99, lockVersion=99).
        PlayerProfile staleIncoming = new PlayerProfile(
                playerId,
                "Updated",
                new RulesetVersion("craftmmo-1.0.1"),
                Map.of(),
                now.plusSeconds(60),
                60L,
                99L,  // stale profileVersion — must NOT propagate
                99L,  // stale lockVersion — must NOT propagate
                now,
                now.plusSeconds(60)
        );

        runner.inTransaction(() -> {
            players.upsert(staleIncoming);
            return null;
        });

        runner.inTransaction(() -> {
            PlayerProfile saved = players.find(playerId).orElseThrow();
            assertEquals("Updated", saved.lastKnownName());
            // Versions must derive from the persisted existing row (0 + 1), not from the stale incoming (99 + 1).
            assertEquals(1L, saved.profileVersion());
            assertEquals(1L, saved.lockVersion());
            return null;
        });
    }

    @Test
    void migrationsCreateExpectedTables() throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     select count(*) from information_schema.tables
                     where table_schema = 'public'
                     and table_name in (
                       'players', 'player_skill_progress', 'player_cooldowns', 'session_ownership',
                       'progression_operations', 'admin_audit_logs'
                     )
                     """);
             var resultSet = statement.executeQuery()) {
            assertTrue(resultSet.next());
            assertEquals(6, resultSet.getInt(1));
        }
    }

    private static PlayerId createPlayer(String name) throws Exception {
        PlayerId playerId = PlayerId.of(UUID.randomUUID());
        Instant now = Instant.parse("2026-06-16T00:00:00Z");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     insert into players (
                         player_uuid, last_known_name, ruleset_version, profile_version, lock_version,
                         last_login_at, playtime_seconds, created_at, updated_at
                     )
                     values (?, ?, 'craftmmo-1.0.0', 0, 0, ?, 0, ?, ?)
                     """)) {
            statement.setObject(1, playerId.value());
            statement.setString(2, name);
            statement.setTimestamp(3, Timestamp.from(now));
            statement.setTimestamp(4, Timestamp.from(now));
            statement.setTimestamp(5, Timestamp.from(now));
            statement.executeUpdate();
        }
        return playerId;
    }
}
