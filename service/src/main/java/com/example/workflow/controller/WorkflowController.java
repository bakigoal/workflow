package com.example.workflow.controller;

import com.example.workflow.core.Signal;
import com.example.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping("/process/{type}")
    public UUID start(@PathVariable String type) {
        return workflowService.start(type);
    }

    @PostMapping("/process/{id}/resume")
    public void resume(@PathVariable UUID id) {
        workflowService.resume(id, Signal.NEXT);
    }
}
