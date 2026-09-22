package com.insurance.lifepremium.application;

import com.insurance.lifepremium.application.dto.CalculationRequest;
import com.insurance.lifepremium.application.dto.RateEntryDto;
import com.insurance.lifepremium.application.service.PremiumCalculationService;
import com.insurance.lifepremium.application.service.ProductService;
import com.insurance.lifepremium.application.service.RateTableService;
import com.insurance.lifepremium.domain.exception.DomainException;
import com.insurance.lifepremium.domain.model.CalculationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CalculationHistoryTest {

    @Autowired
    private PremiumCalculationService calculationService;

    @Autowired
    private ProductService productService;

    @Autowired
    private RateTableService rateTableService;

    private static final String PRODUCT_CODE = "LIFE-WL-HIST";
    private static final String AGENT_ID = "agent-hist-001";
    private static final String OTHER_AGENT_ID = "agent-hist-002";
    private static final BigDecimal RATE = new BigDecimal("0.005000");
    private static final BigDecimal INSURED_AMOUNT = new BigDecimal("1000000");

    @BeforeEach
    void setUp() {
        productService.registerProduct(PRODUCT_CODE, "History Test Product");
        rateTableService.registerRateTable(PRODUCT_CODE);
        RateEntryDto entry = new RateEntryDto();
        entry.setAge(30);
        entry.setPaymentPeriod("ANNUAL");
        entry.setRate(RATE);
        rateTableService.addVersion(PRODUCT_CODE, "v1", List.of(entry));
    }

    // TC-HIST-001: query history returns records
    @Test
    void queryHistory_returnsAgentRecords() {
        performCalculation(AGENT_ID);
        performCalculation(AGENT_ID);

        Page<CalculationRecord> page = calculationService.queryHistory(AGENT_ID, null, null, PageRequest.of(0, 20));
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(r -> r.getAgentId().equals(AGENT_ID));
    }

    // TC-HIST-002: no records found returns empty page
    @Test
    void queryHistory_noRecords_returnsEmptyPage() {
        Page<CalculationRecord> page = calculationService.queryHistory("unknown-agent", null, null, PageRequest.of(0, 20));
        assertThat(page.getTotalElements()).isZero();
    }

    // TC-HIST-005: agent cannot access another agent's record
    @Test
    void getRecord_otherAgentRecord_throwsUnauthorised() {
        var response = performCalculation(AGENT_ID);
        assertThatThrownBy(() -> calculationService.getRecord(response.getRecordId(), OTHER_AGENT_ID))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo("UNAUTHORISED"));
    }

    // TC-HIST-006: pagination boundary
    @Test
    void queryHistory_pagination_firstPageOnly() {
        for (int i = 0; i < 5; i++) {
            performCalculation(AGENT_ID);
        }
        Page<CalculationRecord> page = calculationService.queryHistory(AGENT_ID, null, null, PageRequest.of(0, 2));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(5);
    }

    // TC-HIST-008: tampered agent ID - accessing another agent's record
    @Test
    void getRecord_tamperedAgentId_throwsUnauthorised() {
        var response = performCalculation(AGENT_ID);
        assertThatThrownBy(() -> calculationService.getRecord(response.getRecordId(), "tampered-agent"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo("UNAUTHORISED"));
    }

    private com.insurance.lifepremium.application.dto.CalculationResponse performCalculation(String agentId) {
        CalculationRequest req = new CalculationRequest();
        req.setProductCode(PRODUCT_CODE);
        req.setAge(30);
        req.setInsuredAmount(INSURED_AMOUNT);
        req.setPaymentPeriod("ANNUAL");
        req.setRateVersion("v1");
        return calculationService.calculate(agentId, req);
    }
}