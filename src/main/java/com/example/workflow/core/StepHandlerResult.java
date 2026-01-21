package com.example.workflow.core;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class StepHandlerResult {

    private Signal signal;
    private boolean isPaused;
}
