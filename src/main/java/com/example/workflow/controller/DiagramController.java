package com.example.workflow.controller;

import com.example.workflow.service.WorkflowDiagramGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diagram")
public class DiagramController {

    private final WorkflowDiagramGenerator generator;

    @GetMapping(value = "/{processType}", produces = "text/plain")
    public String diagram(@PathVariable String processType) {
        return generator.generateMermaid(processType);
    }
}