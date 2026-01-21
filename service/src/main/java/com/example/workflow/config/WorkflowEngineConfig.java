package com.example.workflow.config;

import com.example.workflow.core.StepHandler;
import com.example.workflow.core.WorkflowEngine;
import com.example.workflow.core.ports.ProcessStateRepository;
import com.example.workflow.core.ports.StepStateRepository;
import com.example.workflow.core.ports.TransferStateRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

@Configuration
public class WorkflowEngineConfig {

    @Bean
    public WorkflowEngine workflowEngine(ProcessStateRepository processRepo,
                                         StepStateRepository stepRepo,
                                         TransferStateRepository transferRepo,
                                         Function<String, StepHandler> stepHandlerProvider) {
        return new WorkflowEngine(processRepo, stepRepo, transferRepo, stepHandlerProvider);
    }

    @Bean
    public Function<String, StepHandler> stepHandlerProvider(Map<String, StepHandler> handlers) {
        return (String stepType) -> ofNullable(handlers.get(stepType))
                .orElseThrow(() -> new IllegalArgumentException("No handler for " + stepType));
    }

}
