package com.example.workflow.scheduler;

import com.example.workflow.core.Context;
import com.example.workflow.core.Signal;
import com.example.workflow.core.WorkflowEngine;
import com.example.workflow.repository.StepInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowRetryScheduler {

    private final StepInstanceRepository stepRepo;
    private final WorkflowEngine engine;

    @Transactional
    @Scheduled(fixedDelay = 30_000)
    public void retry() {
        log.info("Started retry scheduler");
        while (true) {
            var batch = stepRepo.claimRetryBatch(100);
            if (batch.isEmpty()) {
                break;
            }

            for (var step : batch) {
                engine.execute(
                        new Context().setProcess(step.getProcessInstance()),
                        Signal.RETRY
                );
            }
        }
        log.info("Finished retry scheduler");
    }

    @Transactional
    @Scheduled(fixedDelay = 60_000)
    public void ttlForClaimedSteps() {
        log.info("Started retry claims cleaner");
        stepRepo.clearOldClaims();
        log.info("Finished retry claims cleaner");
    }
}
