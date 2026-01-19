package com.example.workflow.core;

public interface StepHandler {

    /**
     * @return signalCode или null (пауза)
     */
    Signal handle(Context context);
}
