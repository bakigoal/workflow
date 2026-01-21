package com.example.workflow.core.models;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class StepState {

    private UUID id;
    private long version;
    private UUID processId;
    private String stepTypeCode;
    private String transferCode;
    private int retryCount;
    private OffsetDateTime nextRetryAt;
}
