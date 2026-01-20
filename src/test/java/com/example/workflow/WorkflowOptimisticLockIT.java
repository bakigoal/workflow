
package com.example.workflow;

import com.example.workflow.core.Context;
import com.example.workflow.core.Signal;
import com.example.workflow.core.WorkflowEngine;
import com.example.workflow.entity.ProcessInstance;
import com.example.workflow.repository.ProcessInstanceRepository;
import com.example.workflow.steps.TestStepsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestStepsConfig.class)
class WorkflowOptimisticLockIT extends AbstractIntegrationTest {

    @Autowired
    WorkflowEngine engine;

    @Autowired
    ProcessInstanceRepository processRepo;

    @Test
    void shouldHandleConcurrentExecutionSafely() throws Exception {
        var process = new ProcessInstance();
        process.setId(UUID.randomUUID());
        process.setProcessTypeCode("TEST");
        process.setStartTime(OffsetDateTime.now());

        processRepo.save(process);

        var executor = Executors.newFixedThreadPool(2);

        Runnable task = () ->
                engineExecutions(process);

        executor.submit(task);
        executor.submit(task);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        var updated = processRepo.findById(process.getId()).orElseThrow();
        assertThat(updated.getEndTime()).isNotNull();
    }

    private void engineExecutions(ProcessInstance process) {
        // 1 - start
        engine.execute(
                new Context().setProcess(process),
                Signal.START
        );
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
    }
}