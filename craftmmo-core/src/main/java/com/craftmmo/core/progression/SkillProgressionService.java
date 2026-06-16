package com.craftmmo.core.progression;

import com.craftmmo.api.progression.PlayerSkillProgress;
import com.craftmmo.api.time.ClockService;
import com.craftmmo.core.transaction.TransactionRunner;
import java.time.Instant;
import java.util.Objects;

public final class SkillProgressionService {
    private final ProgressionRepository repository;
    private final TransactionRunner transactionRunner;
    private final LevelingPolicy levelingPolicy;
    private final ClockService clockService;

    public SkillProgressionService(
            ProgressionRepository repository,
            TransactionRunner transactionRunner,
            LevelingPolicy levelingPolicy,
            ClockService clockService
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.transactionRunner = Objects.requireNonNull(transactionRunner, "transactionRunner");
        this.levelingPolicy = Objects.requireNonNull(levelingPolicy, "levelingPolicy");
        this.clockService = Objects.requireNonNull(clockService, "clockService");
    }

    public SkillProgressionResult applyXp(SkillProgressionRequest request) {
        return transactionRunner.inTransaction(() -> {
            Instant now = clockService.now();
            OperationRecordResult operationRecord = repository.recordOperationStart(request, now);
            PlayerSkillProgress current = repository.findForUpdate(request.playerId(), request.skillId())
                    .orElseGet(() -> PlayerSkillProgress.initial(request.skillId()));
            if (operationRecord.status() == OperationRecordStatus.DUPLICATE_REPLAY) {
                return new SkillProgressionResult(
                        ProgressionApplyStatus.DUPLICATE_REPLAY,
                        operationRecord.previousResult().orElse(current)
                );
            }
            if (operationRecord.status() == OperationRecordStatus.IDENTITY_CONFLICT) {
                return new SkillProgressionResult(ProgressionApplyStatus.IDENTITY_CONFLICT, current);
            }
            PlayerSkillProgress next = levelingPolicy.applyXp(current, request.xp()).nextVersion();
            if (!repository.save(request.playerId(), current, next, now)) {
                throw new OptimisticLockException("Skill progress changed concurrently");
            }
            repository.markOperationApplied(request.operationId(), next, now);
            return new SkillProgressionResult(ProgressionApplyStatus.APPLIED, next);
        });
    }
}
