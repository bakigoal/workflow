package com.example.workflow.core;

import com.example.workflow.entity.ProcessInstance;
import com.example.workflow.entity.ProcessResult;
import com.example.workflow.entity.StepInstance;
import com.example.workflow.entity.Transfer;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@Accessors(chain = true)
public class Context {
    private ProcessInstance process;
    private Transfer transfer;
    private StepInstance currentStep;
    private ProcessResult result;
}
