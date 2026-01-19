package com.example.workflow.service;

import com.example.workflow.core.Context;
import com.example.workflow.core.Signal;
import com.example.workflow.core.WorkflowEngine;
import com.example.workflow.entity.ProcessInstance;
import com.example.workflow.repository.ProcessInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowEngine engine;
    private final ProcessInstanceRepository processRepo;

    public UUID start(String processType) {
        var p = new ProcessInstance();
        p.setId(UUID.randomUUID());
        p.setProcessTypeCode(processType);
        p.setStartTime(OffsetDateTime.now());

        processRepo.save(p);
        log.info("Created process: {}", p);
        engine.execute(new Context().setProcess(p), Signal.START);
        return p.getId();
    }

    public void resume(UUID processId, Signal signal) {
        var p = processRepo.findById(processId).orElseThrow();
        log.info("[Core]: found process: {}", p);
        engine.execute(new Context().setProcess(p), signal);
    }
}
