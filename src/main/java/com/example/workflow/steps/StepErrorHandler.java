package com.example.workflow.steps;

import com.example.workflow.core.Context;
import com.example.workflow.core.Signal;
import com.example.workflow.core.StepHandler;
import com.example.workflow.entity.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("STEP_ERROR")
public class StepErrorHandler implements StepHandler {

    @Override
    public Signal handle(Context context) {
        log.info("[STEP_ERROR] Error occurred in process: {}", context);
        context.setResult(ProcessResult.ERROR);
        return Signal.FINISH;
    }
}
