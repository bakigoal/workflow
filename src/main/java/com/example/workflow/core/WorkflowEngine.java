package com.example.workflow.core;

import com.example.workflow.config.WorkflowEngineProperties;
import com.example.workflow.entity.ProcessInstance;
import com.example.workflow.entity.ProcessResult;
import com.example.workflow.entity.StepInstance;
import com.example.workflow.entity.Transfer;
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
                t = transferRepo.findStartTransition(p.getProcessTypeCode(), signal.name()).orElseThrow();
                currentStep = createStep(p, t);
            } else {
                var activeStep = activeStepOpt.get();
                t = transferRepo.findStepTransition(p.getProcessTypeCode(), activeStep.getStepTypeCode(), signal.name()).orElseThrow();
                if (signal != Signal.RETRY) {
                    closeStep(activeStep);
                    activeStep = createStep(p, t);
                }
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
            var nextSignal = stepHandler.handle(context);

            log.info("signal: {} step: {}", nextSignal, currentStep.getStepTypeCode());

            // pause
            if (nextSignal == null) {
                log.info("[Core]: Step {} paused", currentStep.getStepTypeCode());
                return; // PAUSE
            }

            // retry
            if (nextSignal == Signal.RETRY) {
                if (scheduleRetry(currentStep)) {
                    log.info("[Core]: Step {} scheduled for retry", currentStep.getStepTypeCode());
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
    }

    private StepInstance createStep(ProcessInstance p, Transfer t) {
        var next = new StepInstance();
        next.setId(UUID.randomUUID());
        next.setProcessInstance(p);
        next.setStepTypeCode(t.getStepTypeCodeTarget());
        next.setTransferCode(t.getCode());
        next.setStartTime(OffsetDateTime.now());

        stepRepo.save(next);
        return next;
    }
}
