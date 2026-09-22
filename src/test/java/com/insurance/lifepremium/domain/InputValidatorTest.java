package com.insurance.lifepremium.domain;

import com.insurance.lifepremium.domain.exception.ValidationException;
import com.insurance.lifepremium.domain.service.InputValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class InputValidatorTest {

    // TC-CALC-004: age at lower bound (0) should pass
    @Test
    void validate_ageLowerBound_passes() {
        assertThatNoException().isThrownBy(() ->
                InputValidator.validate(0, new BigDecimal("500000"), "ANNUAL"));
    }

    // TC-CALC-005: age at upper bound (100) should pass
    @Test
    void validate_ageUpperBound_passes() {
        assertThatNoException().isThrownBy(() ->
                InputValidator.validate(100, new BigDecimal("500000"), "ANNUAL"));
    }

    // TC-CALC-006: insured amount at lower bound should pass
    @Test
    void validate_insuredAmountLowerBound_passes() {
        assertThatNoException().isThrownBy(() ->
                InputValidator.validate(30, new BigDecimal("100000"), "ANNUAL"));
    }

    // TC-CALC-007: insured amount at upper bound should pass
    @Test
    void validate_insuredAmountUpperBound_passes() {
        assertThatNoException().isThrownBy(() ->
                InputValidator.validate(30, new BigDecimal("10000000"), "ANNUAL"));
    }

    // TC-CALC-008: age below minimum
    @Test
    void validate_ageBelowMin_throws() {
        assertThatThrownBy(() ->
                InputValidator.validate(-1, new BigDecimal("500000"), "ANNUAL"))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getViolations()).anyMatch(v -> v.contains("AGE_OUT_OF_RANGE"));
                });
    }

    // TC-CALC-009: age above maximum
    @Test
    void validate_ageAboveMax_throws() {
        assertThatThrownBy(() ->
                InputValidator.validate(101, new BigDecimal("500000"), "ANNUAL"))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getViolations()).anyMatch(v -> v.contains("AGE_OUT_OF_RANGE"));
                });
    }

    // TC-CALC-010: insured amount below minimum
    @Test
    void validate_insuredAmountBelowMin_throws() {
        assertThatThrownBy(() ->
                InputValidator.validate(30, new BigDecimal("99999"), "ANNUAL"))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getViolations()).anyMatch(v -> v.contains("INSURED_AMOUNT_OUT_OF_RANGE"));
                });
    }

    // TC-CALC-011: insured amount above maximum
    @Test
    void validate_insuredAmountAboveMax_throws() {
        assertThatThrownBy(() ->
                InputValidator.validate(30, new BigDecimal("10000001"), "ANNUAL"))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getViolations()).anyMatch(v -> v.contains("INSURED_AMOUNT_OUT_OF_RANGE"));
                });
    }

    // TC-CALC-012: invalid payment period
    @Test
    void validate_invalidPaymentPeriod_throws() {
        assertThatThrownBy(() ->
                InputValidator.validate(30, new BigDecimal("500000"), "WEEKLY"))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getViolations()).anyMatch(v -> v.contains("INVALID_PAYMENT_PERIOD"));
                });
    }

    // TC-CALC-015: multiple violations
    @Test
    void validate_multipleViolations_allReported() {
        assertThatThrownBy(() ->
                InputValidator.validate(-1, new BigDecimal("50"), "INVALID"))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getViolations()).hasSize(3);
                });
    }

    // TC-CALC-018: age = 0 is valid
    @Test
    void validate_ageZero_passes() {
        assertThatNoException().isThrownBy(() ->
                InputValidator.validate(0, new BigDecimal("500000"), "MONTHLY"));
    }
}