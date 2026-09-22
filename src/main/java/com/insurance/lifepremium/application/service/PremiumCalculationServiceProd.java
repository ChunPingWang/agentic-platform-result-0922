package com.insurance.lifepremium.application.service;

import com.insurance.lifepremium.application.dto.CalculationRequest;
import com.insurance.lifepremium.application.dto.CalculationResponse;
import com.insurance.lifepremium.domain.exception.DomainException;
import com.insurance.lifepremium.domain.model.CalculationRecord;
import com.insurance.lifepremium.domain.service.InputValidator;
import com.insurance.lifepremium.domain.service.PremiumCalculator;
import com.insurance.lifepremium.infrastructure.cache.RateCacheService;
import com.insurance.lifepremium.infrastructure.repository.CalculationRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Profile("!test")
public class PremiumCalculationServiceProd {

    private static final Logger log = LoggerFactory.getLogger(PremiumCalculationServiceProd.class);

    private final RateTableService rateTableService;
    private final CalculationRecordRepository calculationRecordRepository;
    private final RateCacheService rateCacheService;

    public PremiumCalculationServiceProd(RateTableService rateTableService,
                                         CalculationRecordRepository calculationRecordRepository,
                                         RateCacheService rateCacheService) {
        this.rateTableService = rateTableService;
        this.calculationRecordRepository = calculationRecordRepository;
        this.rateCacheService = rateCacheService;
    }

    @Transactional
    public CalculationResponse calculate(String agentId, CalculationRequest request) {
        InputValidator.validate(request.getAge(), request.getInsuredAmount(), request.getPaymentPeriod());

        String productCode = request.getProductCode();
        Integer age = request.getAge();
        String paymentPeriod = request.getPaymentPeriod();
        BigDecimal insuredAmount = request.getInsuredAmount();
        String rateVersion = request.getRateVersion() != null ? request.getRateVersion() : "latest";

        BigDecimal rate = resolveRate(productCode, rateVersion, age, paymentPeriod);

        BigDecimal annualPremium = PremiumCalculator.calculateAnnualPremium(insuredAmount, rate);
        BigDecimal monthlyPremium = PremiumCalculator.calculateMonthlyPremium(annualPremium);

        LocalDateTime now = LocalDateTime.now();
        CalculationRecord record = calculationRecordRepository.save(
                new CalculationRecord(agentId, productCode, age, paymentPeriod,
                        insuredAmount, annualPremium, monthlyPremium, rateVersion, now));

        log.info("Premium calculation completed for agent={} product={} recordId={}", agentId, productCode, record.getId());

        return new CalculationResponse(record.getId(), productCode, age, paymentPeriod,
                insuredAmount, annualPremium, monthlyPremium, rateVersion, now);
    }

    private BigDecimal resolveRate(String productCode, String rateVersion, Integer age, String paymentPeriod) {
        Optional<BigDecimal> cached = rateCacheService.get(productCode, rateVersion, age, paymentPeriod);
        if (cached.isPresent()) {
            return cached.get();
        }

        Optional<BigDecimal> dbRate = "latest".equals(rateVersion)
                ? rateTableService.findLatestRate(productCode, age, paymentPeriod)
                : rateTableService.findRate(productCode, rateVersion, age, paymentPeriod);

        BigDecimal rate = dbRate.orElseThrow(() ->
                new DomainException("RATE_NOT_FOUND",
                        "No rate found for product=" + productCode + " version=" + rateVersion
                                + " age=" + age + " period=" + paymentPeriod));

        rateCacheService.put(productCode, rateVersion, age, paymentPeriod, rate);
        return rate;
    }

    @Transactional(readOnly = true)
    public Page<CalculationRecord> queryHistory(String agentId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return calculationRecordRepository.findByAgentIdAndDateRange(agentId, from, to, pageable);
    }

    @Transactional(readOnly = true)
    public CalculationRecord getRecord(Long recordId, String agentId) {
        CalculationRecord record = calculationRecordRepository.findById(recordId)
                .orElseThrow(() -> new DomainException("RECORD_NOT_FOUND", "Record not found: " + recordId));
        if (!record.getAgentId().equals(agentId)) {
            throw new DomainException("UNAUTHORISED", "Access denied to record: " + recordId);
        }
        return record;
    }
}