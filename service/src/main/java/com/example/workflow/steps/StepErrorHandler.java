package com.example.workflow.steps;

import com.example.workflow.core.Context;
import com.example.workflow.core.Signal;
import com.example.workflow.core.StepHandler;
import com.example.workflow.core.StepHandlerResult;
import com.example.workflow.core.models.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("STEP_ERROR")
public class StepErrorHandler implements StepHandler {

    @Override
    public StepHandlerResult handle(Context context) {
        context.setResult(ProcessResult.ERROR);
        return signal(Signal.FINISH);
    }
}
