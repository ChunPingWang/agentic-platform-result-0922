package com.insurance.lifepremium.infrastructure.repository;

import com.insurance.lifepremium.domain.model.RateTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RateTableRepository extends JpaRepository<RateTable, Long> {
    Optional<RateTable> findByProductCode(String productCode);
    boolean existsByProductCode(String productCode);
}