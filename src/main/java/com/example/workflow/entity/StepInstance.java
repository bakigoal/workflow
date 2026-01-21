package com.example.workflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class StepInstance {

    @Id
    private UUID id;

    @Version
    private long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private ProcessInstance processInstance;

    private String stepTypeCode;
    private String transferCode;

    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    private int retryCount;
    private OffsetDateTime nextRetryAt;
    private OffsetDateTime retryClaimedAt;
}
