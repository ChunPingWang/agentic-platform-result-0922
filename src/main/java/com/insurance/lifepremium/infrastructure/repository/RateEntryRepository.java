package com.insurance.lifepremium.infrastructure.repository;

import com.insurance.lifepremium.domain.model.RateEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RateEntryRepository extends JpaRepository<RateEntry, Long> {
    Optional<RateEntry> findByRateTableVersionIdAndAgeAndPaymentPeriod(
            Long versionId, Integer age, String paymentPeriod);
    boolean existsByRateTableVersionIdAndAgeAndPaymentPeriod(
            Long versionId, Integer age, String paymentPeriod);
}