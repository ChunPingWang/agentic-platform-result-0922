package com.insurance.lifepremium.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PremiumCalculator {

    private static final BigDecimal MONTHLY_DIVISOR = BigDecimal.valueOf(12);
    private static final int SCALE = 2;

    private PremiumCalculator() {}

    public static BigDecimal calculateAnnualPremium(BigDecimal insuredAmount, BigDecimal rate) {
        return insuredAmount.multiply(rate).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateMonthlyPremium(BigDecimal annualPremium) {
        return annualPremium.divide(MONTHLY_DIVISOR, SCALE, RoundingMode.HALF_UP);
    }
}