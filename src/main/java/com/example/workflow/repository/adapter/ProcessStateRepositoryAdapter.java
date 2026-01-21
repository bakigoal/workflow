package com.example.workflow.repository.adapter;

import com.example.workflow.core.models.ProcessResult;
import com.example.workflow.core.models.ProcessState;
import com.example.workflow.core.ports.ProcessStateRepository;
import com.example.workflow.mapper.ProcessStateMapper;
import com.example.workflow.repository.ProcessInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProcessStateRepositoryAdapter implements ProcessStateRepository {

    private final ProcessInstanceRepository processInstanceRepository;
    private final ProcessStateMapper processStateMapper;

    @Override
    public Optional<ProcessState> findById(UUID processId) {
        return processInstanceRepository.findById(processId)
                .map(processStateMapper::toState);
    }

    @Override
    public void finish(ProcessState p, ProcessResult result) {
        processInstanceRepository.finish(p.getId(), result.name());
    }
}
