package com.example.workflow.mapper;

import com.example.workflow.core.models.TransferState;
import com.example.workflow.entity.Transfer;
import org.springframework.stereotype.Component;

@Component
public class TransferStateMapper {

    public TransferState toState(Transfer t) {
        return new TransferState()
                .setCode(t.getCode())
                .setTargetStepTypeCode(t.getStepTypeCodeTarget());
    }
}
