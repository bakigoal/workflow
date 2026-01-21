package com.example.workflow.mapper;

import com.example.workflow.core.models.StepState;
import com.example.workflow.entity.StepInstance;
import org.springframework.stereotype.Component;

@Component
public class StepStateMapper {

    public StepState toState(StepInstance s) {
        return new StepState()
                .setId(s.getId())
                .setVersion(s.getVersion())
                .setProcessId(s.getProcessInstance().getId())
                .setStepTypeCode(s.getStepTypeCode())
                .setTransferCode(s.getTransferCode())
                .setRetryCount(s.getRetryCount())
                .setNextRetryAt(s.getNextRetryAt());
    }

    public StepInstance toEntity(StepState s) {
        return new StepInstance()
                .setId(s.getId())
                .setVersion(s.getVersion())
                .setStepTypeCode(s.getStepTypeCode())
                .setTransferCode(s.getTransferCode())
                .setRetryCount(s.getRetryCount())
                .setNextRetryAt(s.getNextRetryAt());
    }
}
