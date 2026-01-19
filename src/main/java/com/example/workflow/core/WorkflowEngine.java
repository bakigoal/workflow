package com.example.workflow.core;

import com.example.workflow.entity.ProcessInstance;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngine {

    private final ProcessInstanceRepository processRepo;
    private final StepInstanceRepository stepRepo;
    private final TransferRepository transferRepo;
    private final StepHandlerRegistry registry;

    @Transactional
    public void execute(Context context, Signal signal) {
        try {
            log.info("[Core]: Start managing process: {}", context.getProcess().getId());
            manageProcess(context, signal);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            log.warn("[Core]: Concurrent processing ignored for process {}", context.getProcess().getId());
        } finally {
            log.info("[Core]: Finish managing process: {}", context.getProcess().getId());
        }
    }

    private void manageProcess(Context context, Signal signal) {
        var p = context.getProcess();

        var currentStepOpt = stepRepo.findFirstByProcessInstance_IdAndEndTimeIsNull(p.getId());
        StepInstance currentStep;
        // ===== START =====
        if (currentStepOpt.isEmpty()) {
            var t = transferRepo.findStartTransition(p.getProcessTypeCode(), signal.name()).orElseThrow();
            context.setTransfer(t);

            if (t.getStepTypeCodeTarget() == null) {
                finishProcess(p);
                return;
            }

            currentStep = createStep(p, t);
        } else {
            currentStep = currentStepOpt.get();
        }


        while (true) {
            context.setCurrentStep(currentStep);

            var stepHandler = registry.get(currentStep.getStepTypeCode());
            var nextSignal = stepHandler.handle(context);

            log.info("signal: {} step: {}", nextSignal, currentStep.getStepTypeCode());

            if (nextSignal == null) {
                log.info("[Core]: Step {} paused", currentStep.getStepTypeCode());
                return;
            }

            // close step
            currentStep.setEndTime(OffsetDateTime.now());
            stepRepo.saveAndFlush(currentStep);

            var t = transferRepo.findStepTransition(p.getProcessTypeCode(), currentStep.getStepTypeCode(), nextSignal.name()).orElseThrow();

            context.setTransfer(t);

            if (t.getStepTypeCodeTarget() == null) {
                finishProcess(p);
                return;
            }

            currentStep = createStep(p, t);
        }
    }

    private void finishProcess(ProcessInstance p) {
        p.setEndTime(OffsetDateTime.now());
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
