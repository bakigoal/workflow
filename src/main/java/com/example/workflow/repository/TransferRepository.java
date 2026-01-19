package com.example.workflow.repository;

import com.example.workflow.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, String> {

    @Query("""
                select t from Transfer t
                where t.processTypeCode = :processTypeCode
                  and t.signalCode = :signal
                  and t.stepTypeCodeSource is null
            """)
    Optional<Transfer> findStartTransition(String processTypeCode, String signal);

    @Query("""
                select t from Transfer t
                where t.processTypeCode = :processTypeCode
                  and t.signalCode = :signal
                  and t.stepTypeCodeSource = :step
            """)
    Optional<Transfer> findStepTransition(String processTypeCode, String step, String signal);
}