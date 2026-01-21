package com.example.workflow.core.models;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ProcessState {

    private UUID id;
    private String processTypeCode;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
}
