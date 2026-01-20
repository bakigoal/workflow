package com.example.workflow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class Transfer {
    @Id
    private String code;

    private String processTypeCode;
    private String stepTypeCodeSource;
    private String stepTypeCodeTarget;
    private String signalCode;
}
