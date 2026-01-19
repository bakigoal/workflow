package com.example.workflow.steps;

import com.example.workflow.core.Context;
import com.example.workflow.core.Signal;
import com.example.workflow.core.StepHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component("STEP_B")
public class StepBHandler implements StepHandler {

    public final Random random = new Random();

    @Override
    public Signal handle(Context context) {
        log.info("[STEP_B] Handling: {}", context);
        return random.nextBoolean() ? Signal.FINISH : Signal.ERROR;
    }
}
