
package com.example.workflow.integration;

import com.example.workflow.config.TestStepsConfig;
import com.example.workflow.core.Context;
import com.example.workflow.core.Signal;
import com.example.workflow.core.WorkflowEngine;
import com.example.workflow.entity.ProcessInstance;
import com.example.workflow.entity.ProcessResult;
import com.example.workflow.repository.ProcessInstanceRepository;
import com.example.workflow.repository.StepInstanceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, TestStepsConfig.class})
class WorkflowOptimisticLockTests extends BaseWorkflow {

    @Autowired
    WorkflowEngine engine;

    @Autowired
    ProcessInstanceRepository processRepo;
    @Autowired
    StepInstanceRepository stepRepo;

    @Test
    void shouldHandleConcurrentExecutionSafely() throws Exception {
        var process = new ProcessInstance();
        process.setId(UUID.randomUUID());
        process.setProcessTypeCode("TEST");
        process.setStartTime(OffsetDateTime.now());

        processRepo.save(process);
        // 1 - start
        engine.execute(
                new Context().setProcess(process),
                Signal.START
        );

        var executor = Executors.newFixedThreadPool(3);

        Runnable task = () -> {
            // 2 - resume
            engine.execute(
                    new Context().setProcess(process),
                    Signal.NEXT
            );
            // 3 - retry
            engine.execute(
                    new Context().setProcess(process),
                    Signal.RETRY
            );
        };

        executor.submit(task);
        executor.submit(task);
        executor.submit(task);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);


        var steps = stepRepo.findAllByProcessInstance_Id(process.getId());
        assertFalse(steps.isEmpty());
        assertEquals(5, steps.size());
        assertSteps(steps, Set.of("TEST_START_S_A", "TEST_S_A_S_B", "TEST_S_B_S_C", "TEST_S_C_S_C", "TEST_S_C_END"));
        var finished = processRepo.findById(process.getId());
        assertTrue(finished.isPresent());
        assertNotNull(finished.get().getEndTime());
        assertEquals(ProcessResult.SUCCESS, finished.get().getResult());
    }
}