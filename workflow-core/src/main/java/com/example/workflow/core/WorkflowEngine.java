package com.example.workflow.core;

import com.example.workflow.core.models.ProcessResult;
import com.example.workflow.core.models.ProcessState;
import com.example.workflow.core.models.StepState;
import com.example.workflow.core.models.TransferState;
import com.example.workflow.core.ports.ProcessStateRepository;
import com.example.workflow.core.ports.StepStateRepository;
import com.example.workflow.core.ports.TransferStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class WorkflowEngine {

    private final ProcessStateRepository processRepo;
    private final StepStateRepository stepRepo;
    private final TransferStateRepository transferRepo;
    private final Function<String, StepHandler> stepHandlerProvider;

    public void execute(UUID processId, Signal signal) {
        try {
            log.info("[Core]: Start managing process: {}", processId);
            var process = processRepo.findById(processId).orElseThrow();
            manageProcess(process, signal);
            log.info("[Core]: Finish managing process: {}", processId);
        } catch (Exception e) {
            log.error("[Core]: Error managing process {}: {}", processId, e.getMessage(), e);
            throw e;
        }
    }


    private void manageProcess(ProcessState p, Signal signal) {
        var context = new Context().setProcess(p);

        while (true) {
            var activeStepOpt = stepRepo.findActiveStep(p.getId());
            TransferState t;
            StepState currentStep;
            if (activeStepOpt.isEmpty()) {
                t = transferRepo.findStart(p.getProcessTypeCode(), signal)
                        .orElseThrow();
                currentStep = createStep(p, t, 0);
            } else {
                var activeStep = activeStepOpt.get();
                t = transferRepo.findNext(p.getProcessTypeCode(), signal, activeStep).orElseThrow();
                closeStep(activeStep);
                activeStep = createStep(p, t, signal == Signal.RETRY ? activeStep.getRetryCount() : 0);
                currentStep = activeStep;
            }
            context.setCurrentStep(currentStep);
            context.setTransfer(t);

            if (t.getTargetStepTypeCode() == null) {
                closeStep(currentStep);
                finishProcess(p, context);
                return;
            }

            var stepHandler = stepHandlerProvider.apply(currentStep.getStepTypeCode());
            log.debug("[Core]: StepHandler [{}] is started", currentStep.getStepTypeCode());
            var result = stepHandler.handle(context);
            log.debug("[Core]: StepHandler [{}] is finished", currentStep.getStepTypeCode());

            // pause
            if (result.isPaused()) {
                log.info("[Core]: Step [{}] paused", currentStep.getStepTypeCode());
                return; // PAUSE
            }

            var nextSignal = result.getSignal();
            log.debug("step: [{}] -> signal: [{}]", currentStep.getStepTypeCode(), nextSignal);

            // retry
            if (nextSignal == Signal.RETRY) {
                if (stepRepo.scheduleForRetry(currentStep)) {
                    log.info("[Core]: Step [{}] scheduled for retry", currentStep.getStepTypeCode());
                    return; // PAUSE
                }

                // retry exhausted → ERROR transition
                nextSignal = Signal.ERROR;
            }

            signal = nextSignal;
        }
    }

    private StepState createStep(ProcessState p, TransferState t, int retryCount) {
        var next = new StepState();
        next.setId(UUID.randomUUID());
        next.setProcessId(p.getId());
        next.setStepTypeCode(t.getTargetStepTypeCode());
        next.setTransferCode(t.getCode());
        next.setRetryCount(retryCount);

        stepRepo.create(next);
        log.debug("[Core]: Step [{}][{}] is created", next.getStepTypeCode(), next.getId());
        return next;
    }

    private void closeStep(StepState oldStep) {
        stepRepo.closeStep(oldStep);
        log.debug("[Core]: Step [{}][{}] is closed", oldStep.getStepTypeCode(), oldStep.getId());
    }

    private void finishProcess(ProcessState p, Context context) {
        var result = Optional.ofNullable(context.getResult()).orElse(ProcessResult.SUCCESS);
        processRepo.finish(p, result);
        log.info("[Core]: Process [{}][{}] is closed with result [{}]", p.getProcessTypeCode(), p.getId(),result.name());
    }
}
