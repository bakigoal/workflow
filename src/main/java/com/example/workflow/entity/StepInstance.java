package com.example.workflow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
public class StepInstance {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private ProcessInstance processInstance;

    private String stepTypeCode;
    private String transferCode;

    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
}
