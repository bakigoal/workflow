package com.example.workflow.core;

import com.example.workflow.core.models.ProcessResult;
import com.example.workflow.core.models.ProcessState;
import com.example.workflow.core.models.StepState;
import com.example.workflow.core.models.TransferState;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Context {
    private ProcessState process;
    private TransferState transfer;
    private StepState currentStep;
    private ProcessResult result;
}
