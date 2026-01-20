package com.example.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "engine")
public class WorkflowEngineProperties {

    private Retry retry;

    @Data
    public static class Retry {
        private int maxRetryCount;
        private int retryAfterSeconds;
        private int retryBatchSize;
        private int claimTtlMinutes;
    }
}
