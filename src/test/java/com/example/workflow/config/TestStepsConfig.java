package com.example.workflow.config;

import com.example.workflow.core.Signal;
import com.example.workflow.core.StepHandler;
import com.example.workflow.core.StepHandlerResult;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class TestStepsConfig {

    @Bean("S_A")
    public StepHandler sa() {
        return context -> new StepHandlerResult().setSignal(Signal.NEXT);
    }

    @Bean("S_B")
    public StepHandler sb() {
        return context -> new StepHandlerResult().setPaused(true);
    }

    @Bean("S_C")
    public StepHandler sc() {
        return context -> {
            if (context.getCurrentStep().getRetryCount() < 1)
                return new StepHandlerResult().setSignal(Signal.RETRY);
            else return new StepHandlerResult().setSignal(Signal.FINISH);
        };
    }

}
