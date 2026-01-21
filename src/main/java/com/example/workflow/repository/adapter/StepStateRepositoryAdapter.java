package com.example.workflow.repository.adapter;

import com.example.workflow.config.WorkflowEngineProperties;
import com.example.workflow.core.models.StepState;
import com.example.workflow.core.ports.StepStateRepository;
import com.example.workflow.entity.StepInstance;
import com.example.workflow.mapper.StepStateMapper;
import com.example.workflow.repository.ProcessInstanceRepository;
import com.example.workflow.repository.StepInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StepStateRepositoryAdapter implements StepStateRepository {

    private final StepInstanceRepository stepInstanceRepository;
    private final ProcessInstanceRepository processInstanceRepository;
    private final StepStateMapper stepStateMapper;
    private final WorkflowEngineProperties engineProps;

    @Override
    public Optional<StepState> findActiveStep(UUID processId) {
        return stepInstanceRepository.findFirstByProcessInstance_IdAndEndTimeIsNull(processId)
                .map(stepStateMapper::toState);
    }

    @Override
    public void create(StepState step) {
        var entity = stepStateMapper.toEntity(step);
        entity.setProcessInstance(processInstanceRepository.findById(step.getProcessId()).orElseThrow());
        entity.setStartTime(OffsetDateTime.now());
        stepInstanceRepository.save(entity);
        log.info("Step is created ({})", entity.getId());
    }

    @Override
    public void closeStep(StepState step) {
        var entity = stepInstanceRepository.findById(step.getId()).orElseThrow();
        entity.setEndTime(OffsetDateTime.now());
        stepInstanceRepository.saveAndFlush(entity);
        log.info("Step is closed ({})", entity.getId());
    }

    @Override
    public boolean scheduleForRetry(StepState step) {
        var entity = stepInstanceRepository.findById(step.getId()).orElseThrow();
        if (entity.getRetryCount() < engineProps.getRetry().getMaxRetryCount()) {
            entity.setRetryCount(entity.getRetryCount() + 1);
            entity.setNextRetryAt(OffsetDateTime.now().plusSeconds(engineProps.getRetry().getRetryAfterSeconds()));

            stepInstanceRepository.save(entity);
            return true;
        }

        return false;
    }
}
