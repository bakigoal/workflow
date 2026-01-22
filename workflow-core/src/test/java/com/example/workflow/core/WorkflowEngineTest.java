package com.example.workflow.core;

import com.example.workflow.core.models.ProcessResult;
import com.example.workflow.core.models.ProcessState;
import com.example.workflow.core.models.StepState;
import com.example.workflow.core.models.TransferState;
import com.example.workflow.core.ports.ProcessStateRepository;
import com.example.workflow.core.ports.StepStateRepository;
import com.example.workflow.core.ports.TransferStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.Mockito.*;

class WorkflowEngineTest {

    WorkflowEngine workflowEngine;

    ProcessStateRepository processRepo;
    StepStateRepository stepRepo;
    TransferStateRepository transferRepo;
    Function<String, StepHandler> stepHandlerProvider;

    private static final UUID PROCESS_ID_ABC = UUID.randomUUID();
    private static final UUID PROCESS_ID_RETRY = UUID.randomUUID();
    private static final UUID PROCESS_ID_ERROR = UUID.randomUUID();
    private static final String PROCESS_TYPE_ABC = "ABC";
    private static final String PROCESS_TYPE_RETRY = "RETRY";
    private static final String PROCESS_TYPE_ERROR = "ERROR";

    @BeforeEach
    void init() {
        processRepo = mock(ProcessStateRepository.class);
        stepRepo = mock(StepStateRepository.class);
        transferRepo = mock(TransferStateRepository.class);
        stepHandlerProvider = getStepHandlerFunction();

        workflowEngine = new WorkflowEngine(processRepo, stepRepo, transferRepo, stepHandlerProvider);
    }

    private static Function<String, StepHandler> getStepHandlerFunction() {
        return stepTypeCode ->
                (Context ctx) -> switch (stepTypeCode) {
                    case "stepA" -> new StepHandlerResult().setSignal(Signal.NEXT);
                    case "stepB" -> new StepHandlerResult().setPaused(true);
                    case "stepC" -> new StepHandlerResult().setSignal(Signal.FINISH);
                    case "stepRetry" -> {
                        if (ctx.getCurrentStep().getRetryCount() < 2) {
                            yield new StepHandlerResult().setSignal(Signal.RETRY);
                        } else {
                            yield new StepHandlerResult().setSignal(Signal.FINISH);
                        }
                    }
                    case null, default -> {
                        ctx.setResult(ProcessResult.ERROR);
                        yield new StepHandlerResult().setSignal(Signal.ERROR);
                    }
                };
    }

    @Test
    void testEngineStartFinish() {
        // given
        when(transferRepo.findStart(PROCESS_TYPE_ABC, Signal.START)).thenReturn(Optional.of(
                new TransferState().setCode("start->A").setTargetStepTypeCode("stepA")
        ));
        when(transferRepo.findNext(PROCESS_TYPE_ABC, Signal.NEXT, "stepA")).thenReturn(Optional.of(
                new TransferState().setCode("A->B").setTargetStepTypeCode("stepB")
        ));
        when(transferRepo.findNext(PROCESS_TYPE_ABC, Signal.NEXT, "stepB")).thenReturn(Optional.of(
                new TransferState().setCode("B->C").setTargetStepTypeCode("stepC")
        ));
        when(transferRepo.findNext(PROCESS_TYPE_ABC, Signal.FINISH, "stepC")).thenReturn(Optional.of(
                new TransferState().setCode("C->end").setTargetStepTypeCode(null)
        ));
        when(stepRepo.findActiveStep(PROCESS_ID_ABC))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new StepState().setStepTypeCode("stepA")))
                .thenReturn(Optional.of(new StepState().setStepTypeCode("stepB")))
                .thenReturn(Optional.of(new StepState().setStepTypeCode("stepC")));

        var process = new ProcessState().setId(PROCESS_ID_ABC).setProcessTypeCode(PROCESS_TYPE_ABC);
        when(processRepo.findById(PROCESS_ID_ABC)).thenReturn(Optional.of(process));

        // when
        workflowEngine.execute(PROCESS_ID_ABC, Signal.START);
        workflowEngine.execute(PROCESS_ID_ABC, Signal.NEXT);

        // then
        verify(processRepo).finish(process, ProcessResult.SUCCESS);
    }

    @Test
    void testEngineStartRetryFinish() {
        // given
        when(transferRepo.findStart(PROCESS_TYPE_RETRY, Signal.START)).thenReturn(Optional.of(
                new TransferState().setCode("start->A").setTargetStepTypeCode("stepA")
        ));
        when(transferRepo.findNext(PROCESS_TYPE_RETRY, Signal.NEXT, "stepA")).thenReturn(Optional.of(
                new TransferState().setCode("A->Retry").setTargetStepTypeCode("stepRetry")
        ));
        when(transferRepo.findNext(PROCESS_TYPE_RETRY, Signal.RETRY, "stepRetry")).thenReturn(Optional.of(
                new TransferState().setCode("Retry->Retry").setTargetStepTypeCode("stepRetry")
        ));
        when(transferRepo.findNext(PROCESS_TYPE_RETRY, Signal.FINISH, "stepRetry")).thenReturn(Optional.of(
                new TransferState().setCode("Retry->end").setTargetStepTypeCode(null)
        ));
        when(stepRepo.findActiveStep(PROCESS_ID_RETRY))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new StepState().setStepTypeCode("stepA")))
                .thenReturn(Optional.of(new StepState().setStepTypeCode("stepRetry").setRetryCount(1)))
                .thenReturn(Optional.of(new StepState().setStepTypeCode("stepRetry").setRetryCount(2)))
        ;
        var process = new ProcessState().setId(PROCESS_ID_RETRY).setProcessTypeCode(PROCESS_TYPE_RETRY);
        when(processRepo.findById(PROCESS_ID_RETRY)).thenReturn(Optional.of(process));
        when(stepRepo.scheduleForRetry(any())).thenReturn(true);

        // when
        workflowEngine.execute(PROCESS_ID_RETRY, Signal.START);
        workflowEngine.execute(PROCESS_ID_RETRY, Signal.RETRY);
        workflowEngine.execute(PROCESS_ID_RETRY, Signal.RETRY);

        // then
        verify(processRepo).finish(process, ProcessResult.SUCCESS);
    }

    @Test
    void testEngineStartError() {
        // given
        when(transferRepo.findStart(PROCESS_TYPE_ERROR, Signal.START)).thenReturn(Optional.of(
                new TransferState().setCode("start->A").setTargetStepTypeCode("stepA")
        ));
        when(transferRepo.findNext(PROCESS_TYPE_ERROR, Signal.NEXT, "stepA")).thenReturn(Optional.of(
                new TransferState().setCode("A->B").setTargetStepTypeCode("stepError")
        ));
        when(transferRepo.findNext(PROCESS_TYPE_ERROR, Signal.ERROR, "stepError")).thenReturn(Optional.of(
                new TransferState().setCode("Error->end").setTargetStepTypeCode(null)
        ));
        when(stepRepo.findActiveStep(PROCESS_ID_ERROR))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new StepState().setStepTypeCode("stepA")))
                .thenReturn(Optional.of(new StepState().setStepTypeCode("stepError")))
        ;
        var process = new ProcessState().setId(PROCESS_ID_ERROR).setProcessTypeCode(PROCESS_TYPE_ERROR);
        when(processRepo.findById(PROCESS_ID_ERROR)).thenReturn(Optional.of(process));

        // when
        workflowEngine.execute(PROCESS_ID_ERROR, Signal.START);

        // then
        verify(processRepo).finish(process, ProcessResult.ERROR);
    }
}