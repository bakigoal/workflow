package com.example.workflow.steps;

import com.example.workflow.core.Context;
import com.example.workflow.core.Signal;
import com.example.workflow.core.StepHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component("STEP_A")
public class StepAHandler implements StepHandler {

    public final Random random = new Random();

    @Override
    public Signal handle(Context context) {
        log.info("[STEP_A] Handling: {}", context);
        return random.nextBoolean() ? null : Signal.NEXT;
    }
}
