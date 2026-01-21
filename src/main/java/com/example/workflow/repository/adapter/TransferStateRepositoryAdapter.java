package com.example.workflow.repository.adapter;

import com.example.workflow.core.Signal;
import com.example.workflow.core.models.StepState;
import com.example.workflow.core.models.TransferState;
import com.example.workflow.core.ports.TransferStateRepository;
import com.example.workflow.mapper.TransferStateMapper;
import com.example.workflow.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TransferStateRepositoryAdapter implements TransferStateRepository {

    private final TransferRepository transferRepository;
    private final TransferStateMapper transferStateMapper;

    @Override
    public Optional<TransferState> findStart(String processTypeCode, Signal signal) {
        return transferRepository.findStartTransition(processTypeCode, signal.name())
                .map(transferStateMapper::toState);
    }

    @Override
    public Optional<TransferState> findNext(String processTypeCode, Signal signal, StepState step) {
        return transferRepository.findStepTransition(processTypeCode, step.getStepTypeCode(), signal.name())
                .map(transferStateMapper::toState);
    }
}
