package com.example.workflow.repository;

import com.example.workflow.entity.StepInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StepInstanceRepository extends JpaRepository<StepInstance, UUID> {
    Optional<StepInstance> findFirstByProcessInstance_IdAndEndTimeIsNull(UUID id);

    @Modifying
    @Query(value = """
            update step_instance
            set retry_claimed_at = now()
            where id in (
                select id
                from step_instance
                where end_time is null
                  and next_retry_at <= now()
                  and retry_claimed_at is null
                order by next_retry_at
                limit :limit
                for update skip locked
            )
            returning *
            """, nativeQuery = true)
    List<StepInstance> claimRetryBatch(int limit);

    @Modifying
    @Query(value = """
            update step_instance
            set retry_claimed_at = null
            where retry_claimed_at < now() - interval '10 minutes';
            """, nativeQuery = true)
    void clearOldClaims();
}
