package com.insurance.lifepremium.domain;

import com.insurance.lifepremium.domain.service.PremiumCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumCalculatorTest {

    @Test
    void calculateAnnualPremium_basicMultiplication() {
        BigDecimal insuredAmount = new BigDecimal("1000000");
        BigDecimal rate = new BigDecimal("0.005000");
        BigDecimal expected = insuredAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        assertThat(PremiumCalculator.calculateAnnualPremium(insuredAmount, rate)).isEqualByComparingTo(expected);
    }

    @Test
    void calculateMonthlyPremium_divideByTwelve() {
        BigDecimal annual = new BigDecimal("5000.00");
        BigDecimal expected = annual.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        assertThat(PremiumCalculator.calculateMonthlyPremium(annual)).isEqualByComparingTo(expected);
    }

    @Test
    void calculateAnnualPremium_largeAmount() {
        BigDecimal insuredAmount = new BigDecimal("10000000");
        BigDecimal rate = new BigDecimal("0.012000");
        BigDecimal expected = insuredAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        assertThat(PremiumCalculator.calculateAnnualPremium(insuredAmount, rate)).isEqualByComparingTo(expected);
    }
}