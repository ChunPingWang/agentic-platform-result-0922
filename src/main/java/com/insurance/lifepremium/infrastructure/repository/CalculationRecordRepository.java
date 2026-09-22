package com.insurance.lifepremium.infrastructure.repository;

import com.insurance.lifepremium.domain.model.CalculationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface CalculationRecordRepository extends JpaRepository<CalculationRecord, Long> {
    Page<CalculationRecord> findByAgentId(String agentId, Pageable pageable);

    @Query("SELECT c FROM CalculationRecord c WHERE c.agentId = :agentId " +
           "AND (:from IS NULL OR c.calculatedAt >= :from) " +
           "AND (:to IS NULL OR c.calculatedAt <= :to)")
    Page<CalculationRecord> findByAgentIdAndDateRange(
            @Param("agentId") String agentId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}