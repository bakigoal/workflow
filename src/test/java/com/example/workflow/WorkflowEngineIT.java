package com.example.workflow;

import com.example.workflow.core.Context;
import com.example.workflow.core.Signal;
import com.example.workflow.core.WorkflowEngine;
import com.example.workflow.entity.ProcessInstance;
import com.example.workflow.entity.ProcessResult;
import com.example.workflow.entity.StepInstance;
import com.example.workflow.repository.ProcessInstanceRepository;
import com.example.workflow.repository.StepInstanceRepository;
import com.example.workflow.repository.TransferRepository;
import com.example.workflow.steps.TestStepsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestStepsConfig.class)
class WorkflowEngineIT {

    @Autowired
    WorkflowEngine workflowEngine;

    @Autowired
    ProcessInstanceRepository processRepo;
    @Autowired
    StepInstanceRepository stepRepo;
    @Autowired
    TransferRepository transferRepository;

    @Test
    @Sql(scripts = "/data/001-test-add-simple-transfers.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldStartAndFinishProcess() {
        // given
        var process = new ProcessInstance();
        process.setId(UUID.randomUUID());
        process.setProcessTypeCode("TEST");
        process.setStartTime(OffsetDateTime.now());

        processRepo.save(process);

        // 1 - start
        workflowEngine.execute(
                new Context().setProcess(process),
                Signal.START
        );
        // 2 - resume
        workflowEngine.execute(
                new Context().setProcess(process),
                Signal.NEXT
        );
        // 3 - retry
        workflowEngine.execute(
                new Context().setProcess(process),
                Signal.RETRY
        );


        var steps = stepRepo.findAllByProcessInstance_Id(process.getId());
        assertFalse(steps.isEmpty());
        assertEquals(5, steps.size());
        assertStepsContains(steps, Set.of("TEST_START_S_A", "TEST_S_A_S_B", "TEST_S_B_S_C", "TEST_S_C_S_C", "TEST_S_C_END"));
        var finished = processRepo.findById(process.getId());
        assertTrue(finished.isPresent());
        assertNotNull(finished.get().getEndTime());
        assertEquals(ProcessResult.SUCCESS, finished.get().getResult());
    }

    private void assertStepsContains(List<StepInstance> steps, Set<String> transferCodes) {
        var stepsTransfers = steps.stream().map(StepInstance::getTransferCode).collect(Collectors.toList());
        for (String transferCode : transferCodes) {
            assertThat(transferCode).isIn(stepsTransfers);
        }
    }
}