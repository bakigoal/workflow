package com.example.workflow.repository;

import com.example.workflow.entity.ProcessInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ProcessInstanceRepository extends JpaRepository<ProcessInstance, UUID> {

    default void finish(UUID id, String result) {
        var processInstance = findById(id).orElseThrow();
        processInstance.setResult(result);
        processInstance.setEndTime(OffsetDateTime.now());
        save(processInstance);
    }
}
