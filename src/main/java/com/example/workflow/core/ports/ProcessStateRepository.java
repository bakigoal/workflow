package com.example.workflow.core.ports;

import com.example.workflow.core.models.ProcessResult;
import com.example.workflow.core.models.ProcessState;

import java.util.Optional;
import java.util.UUID;

public interface ProcessStateRepository {

    Optional<ProcessState> findById(UUID processId);

    void finish(ProcessState p, ProcessResult result);
}
