package com.example.workflow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
public class ProcessInstance {
    @Id
    private UUID id;

    private String processTypeCode;

    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    @Enumerated(value = EnumType.STRING)
    private ProcessResult result;
}
