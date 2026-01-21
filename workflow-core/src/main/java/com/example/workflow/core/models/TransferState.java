package com.example.workflow.core.models;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TransferState {

    private String code;
    private String targetStepTypeCode;
}
