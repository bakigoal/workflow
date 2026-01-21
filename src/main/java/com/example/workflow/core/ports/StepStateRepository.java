package com.example.workflow.core.ports;

import com.example.workflow.core.models.StepState;

import java.util.Optional;
import java.util.UUID;

public interface StepStateRepository {

    Optional<StepState> findActiveStep(UUID processId);

    void create(StepState step);

    void closeStep(StepState step);

    boolean scheduleForRetry(StepState step);
}
