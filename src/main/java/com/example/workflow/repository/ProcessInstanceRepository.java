package com.example.workflow.repository;

import com.example.workflow.entity.ProcessInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessInstanceRepository extends JpaRepository<ProcessInstance, UUID> {
}
