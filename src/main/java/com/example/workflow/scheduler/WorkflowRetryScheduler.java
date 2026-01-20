package com.example.workflow.scheduler;

import com.example.workflow.service.WorkflowRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowRetryScheduler {

    private final WorkflowRetryService retryService;

    @Scheduled(fixedDelay = 30_000)
    public void retry() {
        log.info("Started retry scheduler");
        retryService.doBatchRetry();
        log.info("Finished retry scheduler");
    }

    @Scheduled(fixedDelay = 60_000)
    public void ttlForClaimedSteps() {
        log.info("Started retry claims cleaner");
        retryService.clearOldClaims();
        log.info("Finished retry claims cleaner");
    }
}
