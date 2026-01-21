package com.example.workflow.core;

public interface StepHandler {

    /**
     * @return signalCode или null (пауза)
     */
    StepHandlerResult handle(Context context);

    default StepHandlerResult pause() {
        return new StepHandlerResult().setPaused(true);
    }

    default StepHandlerResult signal(Signal signal) {
        return new StepHandlerResult().setSignal(signal);
    }
}
