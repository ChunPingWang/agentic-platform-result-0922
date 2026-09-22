package com.insurance.lifepremium.infrastructure.repository;

import com.insurance.lifepremium.domain.model.RateTableVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RateTableVersionRepository extends JpaRepository<RateTableVersion, Long> {
    Optional<RateTableVersion> findByRateTableIdAndVersion(Long rateTableId, String version);
    boolean existsByRateTableIdAndVersion(Long rateTableId, String version);
}