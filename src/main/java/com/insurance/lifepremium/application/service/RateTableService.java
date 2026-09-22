package com.insurance.lifepremium.application.service;

import com.insurance.lifepremium.application.dto.RateEntryDto;
import com.insurance.lifepremium.domain.exception.DomainException;
import com.insurance.lifepremium.domain.model.RateEntry;
import com.insurance.lifepremium.domain.model.RateTable;
import com.insurance.lifepremium.domain.model.RateTableVersion;
import com.insurance.lifepremium.infrastructure.repository.RateEntryRepository;
import com.insurance.lifepremium.infrastructure.repository.RateTableRepository;
import com.insurance.lifepremium.infrastructure.repository.RateTableVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class RateTableService {

    private final RateTableRepository rateTableRepository;
    private final RateTableVersionRepository rateTableVersionRepository;
    private final RateEntryRepository rateEntryRepository;
    private final ProductService productService;

    public RateTableService(RateTableRepository rateTableRepository,
                            RateTableVersionRepository rateTableVersionRepository,
                            RateEntryRepository rateEntryRepository,
                            ProductService productService) {
        this.rateTableRepository = rateTableRepository;
        this.rateTableVersionRepository = rateTableVersionRepository;
        this.rateEntryRepository = rateEntryRepository;
        this.productService = productService;
    }

    @Transactional
    public RateTable registerRateTable(String productCode) {
        // Ensure product exists
        productService.getProduct(productCode);
        if (rateTableRepository.existsByProductCode(productCode)) {
            throw new DomainException("DUPLICATE_RATE_TABLE", "Rate table already registered for product: " + productCode);
        }
        return rateTableRepository.save(new RateTable(productCode));
    }

    @Transactional
    public RateTableVersion addVersion(String productCode, String version, List<RateEntryDto> entries) {
        RateTable rateTable = rateTableRepository.findByProductCode(productCode)
                .orElseThrow(() -> new DomainException("RATE_TABLE_NOT_FOUND", "Rate table not found for product: " + productCode));

        if (rateTableVersionRepository.existsByRateTableIdAndVersion(rateTable.getId(), version)) {
            throw new DomainException("DUPLICATE_VERSION", "Version already exists: " + version);
        }

        RateTableVersion rtv = rateTableVersionRepository.save(new RateTableVersion(rateTable, version));

        if (entries != null) {
            for (RateEntryDto dto : entries) {
                if (rateEntryRepository.existsByRateTableVersionIdAndAgeAndPaymentPeriod(
                        rtv.getId(), dto.getAge(), dto.getPaymentPeriod())) {
                    throw new DomainException("DUPLICATE_RATE_ENTRY",
                            "Duplicate rate entry for age=" + dto.getAge() + " period=" + dto.getPaymentPeriod());
                }
                rateEntryRepository.save(new RateEntry(rtv, dto.getAge(), dto.getPaymentPeriod(), dto.getRate()));
            }
        }
        return rtv;
    }

    @Transactional(readOnly = true)
    public Optional<BigDecimal> findRate(String productCode, String version, Integer age, String paymentPeriod) {
        return rateTableRepository.findByProductCode(productCode)
                .flatMap(rt -> rateTableVersionRepository.findByRateTableIdAndVersion(rt.getId(), version))
                .flatMap(rtv -> rateEntryRepository.findByRateTableVersionIdAndAgeAndPaymentPeriod(
                        rtv.getId(), age, paymentPeriod))
                .map(RateEntry::getRate);
    }

    @Transactional(readOnly = true)
    public Optional<BigDecimal> findLatestRate(String productCode, Integer age, String paymentPeriod) {
        return rateTableRepository.findByProductCode(productCode)
                .flatMap(rt -> {
                    List<RateTableVersion> versions = rateTableVersionRepository
                            .findAll()
                            .stream()
                            .filter(v -> v.getRateTable().getId().equals(rt.getId()))
                            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                            .toList();
                    return versions.stream()
                            .flatMap(v -> rateEntryRepository
                                    .findByRateTableVersionIdAndAgeAndPaymentPeriod(v.getId(), age, paymentPeriod)
                                    .stream())
                            .findFirst();
                })
                .map(RateEntry::getRate);
    }
}