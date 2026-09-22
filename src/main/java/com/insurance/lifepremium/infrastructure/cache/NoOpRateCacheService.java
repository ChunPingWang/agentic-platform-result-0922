package com.insurance.lifepremium.infrastructure.cache;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Profile("test")
public class NoOpRateCacheService {

    public Optional<BigDecimal> get(String productCode, String version, Integer age, String paymentPeriod) {
        return Optional.empty();
    }

    public void put(String productCode, String version, Integer age, String paymentPeriod, BigDecimal rate) {
        // no-op
    }
}