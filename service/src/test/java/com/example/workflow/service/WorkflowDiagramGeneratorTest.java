package com.example.workflow.service;

import com.example.workflow.entity.Transfer;
import com.example.workflow.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowDiagramGeneratorTest {

    WorkflowDiagramGenerator generator;
    TransferRepository transferRepository;

    @BeforeEach
    void init() {
        generator = new WorkflowDiagramGenerator(
                transferRepository = mock(TransferRepository.class)
        );
    }

    @Test
    void testDiagram() {
        when(transferRepository.findByProcessTypeCode("TEST")).thenReturn(List.of(
                new Transfer().setStepTypeCodeSource(null).setStepTypeCodeTarget("STEP_A").setSignalCode("START"),
                new Transfer().setStepTypeCodeSource("STEP_A").setStepTypeCodeTarget("STEP_B").setSignalCode("NEXT"),
                new Transfer().setStepTypeCodeSource("STEP_B").setStepTypeCodeTarget("STEP_B").setSignalCode("RETRY"),
                new Transfer().setStepTypeCodeSource("STEP_B").setStepTypeCodeTarget(null).setSignalCode("FINISH"),
                new Transfer().setStepTypeCodeSource("STEP_B").setStepTypeCodeTarget("STEP_ERROR").setSignalCode("ERROR"),
                new Transfer().setStepTypeCodeSource("STEP_ERROR").setStepTypeCodeTarget(null).setSignalCode("FINISH")
        ));

        var actual = generator.generateMermaid("TEST");

        var expected = """
                stateDiagram-v2
                    [*] --> STEP_A : START
                    STEP_A --> STEP_B : NEXT
                    STEP_B --> STEP_B : RETRY
                    STEP_B --> [*] : FINISH
                    STEP_B --> STEP_ERROR : ERROR
                    STEP_ERROR --> [*] : FINISH
                """.trim();
        assertEquals(expected, actual);
    }
}