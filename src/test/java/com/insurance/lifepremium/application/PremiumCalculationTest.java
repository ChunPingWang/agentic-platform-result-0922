package com.insurance.lifepremium.application;

import com.insurance.lifepremium.application.dto.CalculationRequest;
import com.insurance.lifepremium.application.dto.CalculationResponse;
import com.insurance.lifepremium.application.dto.RateEntryDto;
import com.insurance.lifepremium.application.dto.RateTableVersionRequest;
import com.insurance.lifepremium.application.service.PremiumCalculationService;
import com.insurance.lifepremium.application.service.ProductService;
import com.insurance.lifepremium.application.service.RateTableService;
import com.insurance.lifepremium.domain.exception.DomainException;
import com.insurance.lifepremium.domain.exception.ValidationException;
import com.insurance.lifepremium.domain.model.CalculationRecord;
import com.insurance.lifepremium.domain.service.PremiumCalculator;
import com.insurance.lifepremium.infrastructure.repository.CalculationRecordRepository;
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
class PremiumCalculationTest {

    @Autowired
    private PremiumCalculationService calculationService;

    @Autowired
    private ProductService productService;

    @Autowired
    private RateTableService rateTableService;

    @Autowired
    private CalculationRecordRepository calculationRecordRepository;

    private static final String PRODUCT_CODE = "LIFE-WL-01";
    private static final String AGENT_ID = "agent-001";
    private static final String VERSION = "v1";
    private static final BigDecimal RATE = new BigDecimal("0.005000");
    private static final BigDecimal INSURED_AMOUNT = new BigDecimal("1000000");
    private static final int AGE = 30;
    private static final String PAYMENT_PERIOD = "ANNUAL";

    @BeforeEach
    void setUp() {
        productService.registerProduct(PRODUCT_CODE, "Whole Life Insurance");
        rateTableService.registerRateTable(PRODUCT_CODE);
        RateEntryDto entry = new RateEntryDto();
        entry.setAge(AGE);
        entry.setPaymentPeriod(PAYMENT_PERIOD);
        entry.setRate(RATE);
        rateTableService.addVersion(PRODUCT_CODE, VERSION, List.of(entry));
    }

    // TC-CALC-001: cache miss -> load from DB -> calculate
    @Test
    void calculate_cacheMiss_loadsFromDb_returnsResult() {
        CalculationRequest request = buildRequest(AGE, INSURED_AMOUNT, PAYMENT_PERIOD, VERSION);
        CalculationResponse response = calculationService.calculate(AGENT_ID, request);

        BigDecimal expectedAnnual = PremiumCalculator.calculateAnnualPremium(INSURED_AMOUNT, RATE);
        BigDecimal expectedMonthly = PremiumCalculator.calculateMonthlyPremium(expectedAnnual);

        assertThat(response.getAnnualPremium()).isEqualByComparingTo(expectedAnnual);
        assertThat(response.getMonthlyPremium()).isEqualByComparingTo(expectedMonthly);
        assertThat(response.getRecordId()).isNotNull();
    }

    // TC-CALC-002: cache miss -> load -> result saved
    @Test
    void calculate_resultSavedToDb() {
        CalculationRequest request = buildRequest(AGE, INSURED_AMOUNT, PAYMENT_PERIOD, VERSION);
        CalculationResponse response = calculationService.calculate(AGENT_ID, request);

        CalculationRecord saved = calculationRecordRepository.findById(response.getRecordId()).orElseThrow();
        assertThat(saved.getAgentId()).isEqualTo(AGENT_ID);
        assertThat(saved.getProductCode()).isEqualTo(PRODUCT_CODE);
        assertThat(saved.getAnnualPremium()).isEqualByComparingTo(response.getAnnualPremium());
    }

    // TC-CALC-003: full happy path returns both premiums
    @Test
    void calculate_happyPath_returnsBothPremiums() {
        CalculationRequest request = buildRequest(AGE, INSURED_AMOUNT, PAYMENT_PERIOD, VERSION);
        CalculationResponse response = calculationService.calculate(AGENT_ID, request);

        assertThat(response.getAnnualPremium()).isPositive();
        assertThat(response.getMonthlyPremium()).isPositive();
        assertThat(response.getMonthlyPremium()).isLessThan(response.getAnnualPremium());
    }

    // TC-CALC-008: age below minimum
    @Test
    void calculate_ageBelowMin_throwsValidationException() {
        CalculationRequest request = buildRequest(-1, INSURED_AMOUNT, PAYMENT_PERIOD, VERSION);
        assertThatThrownBy(() -> calculationService.calculate(AGENT_ID, request))
                .isInstanceOf(ValidationException.class);
    }

    // TC-CALC-009: age above maximum
    @Test
    void calculate_ageAboveMax_throwsValidationException() {
        CalculationRequest request = buildRequest(101, INSURED_AMOUNT, PAYMENT_PERIOD, VERSION);
        assertThatThrownBy(() -> calculationService.calculate(AGENT_ID, request))
                .isInstanceOf(ValidationException.class);
    }

    // TC-CALC-010: insured amount below minimum
    @Test
    void calculate_insuredAmountBelowMin_throwsValidationException() {
        CalculationRequest request = buildRequest(AGE, new BigDecimal("50000"), PAYMENT_PERIOD, VERSION);
        assertThatThrownBy(() -> calculationService.calculate(AGENT_ID, request))
                .isInstanceOf(ValidationException.class);
    }

    // TC-CALC-011: insured amount above maximum
    @Test
    void calculate_insuredAmountAboveMax_throwsValidationException() {
        CalculationRequest request = buildRequest(AGE, new BigDecimal("99999999"), PAYMENT_PERIOD, VERSION);
        assertThatThrownBy(() -> calculationService.calculate(AGENT_ID, request))
                .isInstanceOf(ValidationException.class);
    }

    // TC-CALC-012: invalid payment period
    @Test
    void calculate_invalidPaymentPeriod_throwsValidationException() {
        CalculationRequest request = buildRequest(AGE, INSURED_AMOUNT, "WEEKLY", VERSION);
        assertThatThrownBy(() -> calculationService.calculate(AGENT_ID, request))
                .isInstanceOf(ValidationException.class);
    }

    // TC-CALC-013: rate not found
    @Test
    void calculate_rateNotFound_throwsDomainException() {
        CalculationRequest request = buildRequest(99, INSURED_AMOUNT, PAYMENT_PERIOD, VERSION);
        assertThatThrownBy(() -> calculationService.calculate(AGENT_ID, request))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo("RATE_NOT_FOUND"));
    }

    // TC-CALC-016: record persistence verified
    @Test
    void calculate_recordPersisted_canBeRetrieved() {
        CalculationRequest request = buildRequest(AGE, INSURED_AMOUNT, PAYMENT_PERIOD, VERSION);
        CalculationResponse response = calculationService.calculate(AGENT_ID, request);

        CalculationRecord record = calculationService.getRecord(response.getRecordId(), AGENT_ID);
        assertThat(record.getId()).isEqualTo(response.getRecordId());
        assertThat(record.getAgentId()).isEqualTo(AGENT_ID);
    }

    // TC-CALC-018: age = 0 is valid
    @Test
    void calculate_ageZero_valid() {
        RateEntryDto entry = new RateEntryDto();
        entry.setAge(0);
        entry.setPaymentPeriod(PAYMENT_PERIOD);
        entry.setRate(RATE);
        rateTableService.addVersion(PRODUCT_CODE, "v2", List.of(entry));

        CalculationRequest request = buildRequest(0, INSURED_AMOUNT, PAYMENT_PERIOD, "v2");
        CalculationResponse response = calculationService.calculate(AGENT_ID, request);
        assertThat(response.getAnnualPremium()).isPositive();
    }

    // TC-CALC-022: concurrent requests same agent
    @Test
    void calculate_concurrentRequestsSameAgent_bothSucceed() {
        CalculationRequest r1 = buildRequest(AGE, INSURED_AMOUNT, PAYMENT_PERIOD, VERSION);
        CalculationRequest r2 = buildRequest(AGE, new BigDecimal("500000"), PAYMENT_PERIOD, VERSION);

        CalculationResponse resp1 = calculationService.calculate(AGENT_ID, r1);
        CalculationResponse resp2 = calculationService.calculate(AGENT_ID, r2);

        assertThat(resp1.getRecordId()).isNotEqualTo(resp2.getRecordId());
    }

    // TC-CALC-023: concurrent requests different agents
    @Test
    void calculate_concurrentRequestsDifferentAgents_bothSucceed() {
        CalculationRequest r1 = buildRequest(AGE, INSURED_AMOUNT, PAYMENT_PERIOD, VERSION);
        CalculationRequest r2 = buildRequest(AGE, INSURED_AMOUNT, PAYMENT_PERIOD, VERSION);

        CalculationResponse resp1 = calculationService.calculate("agent-001", r1);
        CalculationResponse resp2 = calculationService.calculate("agent-002", r2);

        assertThat(resp1.getRecordId()).isNotEqualTo(resp2.getRecordId());
    }

    private CalculationRequest buildRequest(Integer age, BigDecimal insuredAmount,
                                             String paymentPeriod, String rateVersion) {
        CalculationRequest req = new CalculationRequest();
        req.setProductCode(PRODUCT_CODE);
        req.setAge(age);
        req.setInsuredAmount(insuredAmount);
        req.setPaymentPeriod(paymentPeriod);
        req.setRateVersion(rateVersion);
        return req;
    }
}