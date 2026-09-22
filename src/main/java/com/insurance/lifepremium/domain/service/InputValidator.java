package com.insurance.lifepremium.domain.service;

import com.insurance.lifepremium.domain.exception.ValidationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InputValidator {

    public static final int AGE_MIN = 0;
    public static final int AGE_MAX = 100;
    public static final BigDecimal INSURED_AMOUNT_MIN = BigDecimal.valueOf(100_000);
    public static final BigDecimal INSURED_AMOUNT_MAX = BigDecimal.valueOf(10_000_000);
    public static final Set<String> VALID_PAYMENT_PERIODS = Set.of("ANNUAL", "MONTHLY", "QUARTERLY", "SEMI_ANNUAL");

    private InputValidator() {}

    public static void validate(Integer age, BigDecimal insuredAmount, String paymentPeriod) {
        List<String> violations = new ArrayList<>();

        if (age == null || age < AGE_MIN || age > AGE_MAX) {
            violations.add("AGE_OUT_OF_RANGE: age must be between " + AGE_MIN + " and " + AGE_MAX);
        }
        if (insuredAmount == null
                || insuredAmount.compareTo(INSURED_AMOUNT_MIN) < 0
                || insuredAmount.compareTo(INSURED_AMOUNT_MAX) > 0) {
            violations.add("INSURED_AMOUNT_OUT_OF_RANGE: insuredAmount must be between "
                    + INSURED_AMOUNT_MIN + " and " + INSURED_AMOUNT_MAX);
        }
        if (paymentPeriod == null || !VALID_PAYMENT_PERIODS.contains(paymentPeriod)) {
            violations.add("INVALID_PAYMENT_PERIOD: paymentPeriod must be one of " + VALID_PAYMENT_PERIODS);
        }

        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }
}