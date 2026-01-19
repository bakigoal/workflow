package com.example.workflow.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class StepHandlerRegistry {

    private final Map<String, StepHandler> handlers;

    public StepHandler get(String stepType) {
        return ofNullable(handlers.get(stepType))
                .orElseThrow(() -> new IllegalArgumentException("No handler for " + stepType));
    }
}
