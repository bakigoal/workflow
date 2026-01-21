package com.example.workflow.core;

import com.example.workflow.config.WorkflowEngineProperties;
import com.example.workflow.entity.ProcessInstance;
import com.example.workflow.entity.ProcessResult;
import com.example.workflow.entity.StepInstance;
import com.example.workflow.entity.Transfer;
import com.example.workflow.exceptions.ApiError;
import com.example.workflow.exceptions.GeneralExceptionContainer;
import com.example.workflow.repository.ProcessInstanceRepository;
import com.example.workflow.repository.StepInstanceRepository;
import com.example.workflow.repository.TransferRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngine {

    private final ProcessInstanceRepository processRepo;
    private final StepInstanceRepository stepRepo;
    private final TransferRepository transferRepo;
    private final StepHandlerRegistry registry;
    private final WorkflowEngineProperties engineProps;

    @Transactional
    public void execute(Context context, Signal signal) {
        try {
            log.info("[Core]: Start managing process: {}", context.getProcess().getId());
            manageProcess(context, signal);
            log.info("[Core]: Finish managing process: {}", context.getProcess().getId());
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            log.warn("[Core]: Concurrent processing ignored for process {}", context.getProcess().getId());
        }
    }

    private void manageProcess(Context context, Signal signal) {
        var p = context.getProcess();

        while (true) {
            var activeStepOpt = stepRepo.findFirstByProcessInstance_IdAndEndTimeIsNull(p.getId());
            Transfer t;
            StepInstance currentStep;
            if (activeStepOpt.isEmpty()) {
                t = transferRepo.findStartTransition(p.getProcessTypeCode(), signal.name())
                        .orElseThrow(() -> new GeneralExceptionContainer(ApiError.ERROR_TRANSFER_NOT_FOUND));
                currentStep = createStep(p, t, 0);
            } else {
                var activeStep = activeStepOpt.get();
                t = transferRepo.findStepTransition(p.getProcessTypeCode(), activeStep.getStepTypeCode(), signal.name())
                        .orElseThrow(() -> new GeneralExceptionContainer(ApiError.ERROR_TRANSFER_NOT_FOUND));
                closeStep(activeStep);
                activeStep = createStep(p, t, signal == Signal.RETRY ? activeStep.getRetryCount() : 0);
                currentStep = activeStep;
            }
            context.setCurrentStep(currentStep);
            context.setTransfer(t);

            if (t.getStepTypeCodeTarget() == null) {
                closeStep(currentStep);
                finishProcess(p, context);
                return;
            }

            var stepHandler = registry.get(currentStep.getStepTypeCode());
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
                if (scheduleRetry(currentStep)) {
                    log.info("[Core]: Step [{}] scheduled for retry", currentStep.getStepTypeCode());
                    return; // PAUSE
                }

                // retry exhausted → ERROR transition
                nextSignal = Signal.ERROR;
            }

            signal = nextSignal;
        }
    }

    private void closeStep(StepInstance oldStep) {
        oldStep.setEndTime(OffsetDateTime.now());
        stepRepo.saveAndFlush(oldStep);
        log.debug("[Core]: Step [{}][{}] is closed", oldStep.getStepTypeCode(), oldStep.getId());
    }

    private boolean scheduleRetry(StepInstance currentStep) {
        if (currentStep.getRetryCount() < engineProps.getRetry().getMaxRetryCount()) {
            currentStep.setRetryCount(currentStep.getRetryCount() + 1);
            currentStep.setNextRetryAt(OffsetDateTime.now().plusSeconds(engineProps.getRetry().getRetryAfterSeconds()));

            stepRepo.save(currentStep);
            return true;
        }

        return false;
    }

    private void finishProcess(ProcessInstance p, Context context) {
        p.setEndTime(OffsetDateTime.now());
        p.setResult(Optional.ofNullable(context.getResult()).orElse(ProcessResult.SUCCESS));
        processRepo.save(p);
        log.info("[Core]: Process [{}][{}] is closed with result [{}]", p.getProcessTypeCode(), p.getId(), p.getResult().name());
    }

    private StepInstance createStep(ProcessInstance p, Transfer t, int retryCount) {
        var next = new StepInstance();
        next.setId(UUID.randomUUID());
        next.setProcessInstance(p);
        next.setStepTypeCode(t.getStepTypeCodeTarget());
        next.setTransferCode(t.getCode());
        next.setStartTime(OffsetDateTime.now());
        next.setRetryCount(retryCount);

        stepRepo.save(next);
        log.debug("[Core]: Step [{}][{}] is created", next.getStepTypeCode(), next.getId());
        return next;
    }
}
