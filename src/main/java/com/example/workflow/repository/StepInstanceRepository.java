package com.example.workflow.repository;

import com.example.workflow.entity.StepInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StepInstanceRepository extends JpaRepository<StepInstance, UUID> {
    Optional<StepInstance> findFirstByProcessInstance_IdAndEndTimeIsNull(UUID id);
}
