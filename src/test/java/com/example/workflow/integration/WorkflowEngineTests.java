package com.example.workflow.integration;

import com.example.workflow.config.PostgresTestcontainersConfiguration;
import com.example.workflow.config.TestStepsConfig;
import com.example.workflow.core.Signal;
import com.example.workflow.core.WorkflowEngine;
import com.example.workflow.core.models.ProcessResult;
import com.example.workflow.entity.ProcessInstance;
import com.example.workflow.repository.ProcessInstanceRepository;
import com.example.workflow.repository.StepInstanceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({PostgresTestcontainersConfiguration.class, TestStepsConfig.class})
class WorkflowEngineTests extends BaseWorkflow {

    @Autowired
    WorkflowEngine workflowEngine;

    @Autowired
    ProcessInstanceRepository processRepo;
    @Autowired
    StepInstanceRepository stepRepo;

    @Test
    void shouldStartAndFinishProcess() {
        // given
        var process = new ProcessInstance();
        process.setId(UUID.randomUUID());
        process.setProcessTypeCode("TEST");
        process.setStartTime(OffsetDateTime.now());

        processRepo.save(process);

        // 1 - start
        workflowEngine.execute(
                process.getId(),
                Signal.START
        );
        // 2 - resume
        workflowEngine.execute(
                process.getId(),
                Signal.NEXT
        );
        // 3 - retry
        workflowEngine.execute(
                process.getId(),
                Signal.RETRY
        );


        var steps = stepRepo.findAllByProcessInstance_Id(process.getId());
        assertFalse(steps.isEmpty());
        assertEquals(5, steps.size());
        assertSteps(steps, Set.of("TEST_START_S_A", "TEST_S_A_S_B", "TEST_S_B_S_C", "TEST_S_C_S_C", "TEST_S_C_END"));
        var finished = processRepo.findById(process.getId());
        assertTrue(finished.isPresent());
        assertNotNull(finished.get().getEndTime());
        assertEquals(ProcessResult.SUCCESS.name(), finished.get().getResult());
    }
}