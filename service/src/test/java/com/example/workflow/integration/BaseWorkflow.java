package com.example.workflow.integration;

import com.example.workflow.entity.StepInstance;
import org.assertj.core.api.AssertionsForClassTypes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

abstract class BaseWorkflow {

    protected void assertSteps(List<StepInstance> steps, Set<String> transferCodes) {
        var stepsTransfers = steps.stream().map(StepInstance::getTransferCode).collect(Collectors.toList());
        for (String transferCode : transferCodes) {
            AssertionsForClassTypes.assertThat(transferCode).isIn(stepsTransfers);
        }
        for (String transferCode : stepsTransfers) {
            AssertionsForClassTypes.assertThat(transferCode).isIn(transferCodes);
        }
    }
}
