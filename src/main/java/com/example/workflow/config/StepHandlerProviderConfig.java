package com.example.workflow.config;

import com.example.workflow.core.StepHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

@Configuration(proxyBeanMethods = false)
public class StepHandlerProviderConfig {

    @Bean
    public Function<String, StepHandler> stepHandlerProvider(Map<String, StepHandler> handlers) {
        return (String stepType) -> ofNullable(handlers.get(stepType))
                .orElseThrow(() -> new IllegalArgumentException("No handler for " + stepType));
    }

}
