package com.example.workflow.service;

import com.example.workflow.config.WorkflowEngineProperties;
import com.example.workflow.core.Context;
import com.example.workflow.core.Signal;
import com.example.workflow.core.WorkflowEngine;
import com.example.workflow.repository.StepInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WorkflowRetryService {

    private final StepInstanceRepository stepRepo;
    private final WorkflowEngine engine;
    private final WorkflowEngineProperties engineProps;

    public void doBatchRetry() {
        while (true) {
            var batch = stepRepo.claimRetryBatch(engineProps.getRetry().getRetryBatchSize());
            if (batch.isEmpty()) {
                break;
            }

            for (var step : batch) {
                engine.execute(
                        new Context().setProcess(step.getProcessInstance()),
                        Signal.RETRY
                );
            }

            stepRepo.saveAll(batch.stream().peek(it -> it.setRetryClaimedAt(null)).toList());
        }
    }

    public void clearOldClaims() {
        stepRepo.clearOldClaims(engineProps.getRetry().getClaimTtlMinutes());
    }
}
