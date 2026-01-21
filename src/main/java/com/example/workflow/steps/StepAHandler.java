package com.example.workflow.steps;

import com.example.workflow.core.Context;
import com.example.workflow.core.StepHandler;
import com.example.workflow.core.StepHandlerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("STEP_A")
public class StepAHandler implements StepHandler {

    @Override
    public StepHandlerResult handle(Context context) {
        return pause();
    }
}
