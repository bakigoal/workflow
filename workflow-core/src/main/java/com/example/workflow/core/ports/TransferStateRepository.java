package com.example.workflow.core.ports;

import com.example.workflow.core.Signal;
import com.example.workflow.core.models.StepState;
import com.example.workflow.core.models.TransferState;

import java.util.Optional;

public interface TransferStateRepository {

    Optional<TransferState> findStart(String processTypeCode, Signal signal);

    Optional<TransferState> findNext(String processTypeCode, Signal signal, String stepTypeCode);
}
