package com.insurance.lifepremium.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CalculationResponse {
    private Long recordId;
    private String productCode;
    private Integer age;
    private String paymentPeriod;
    private BigDecimal insuredAmount;
    private BigDecimal annualPremium;
    private BigDecimal monthlyPremium;
    private String rateVersion;
    private LocalDateTime calculatedAt;

    public CalculationResponse() {}

    public CalculationResponse(Long recordId, String productCode, Integer age, String paymentPeriod,
                                BigDecimal insuredAmount, BigDecimal annualPremium,
                                BigDecimal monthlyPremium, String rateVersion, LocalDateTime calculatedAt) {
        this.recordId = recordId;
        this.productCode = productCode;
        this.age = age;
        this.paymentPeriod = paymentPeriod;
        this.insuredAmount = insuredAmount;
        this.annualPremium = annualPremium;
        this.monthlyPremium = monthlyPremium;
        this.rateVersion = rateVersion;
        this.calculatedAt = calculatedAt;
    }

    public Long getRecordId() { return recordId; }
    public String getProductCode() { return productCode; }
    public Integer getAge() { return age; }
    public String getPaymentPeriod() { return paymentPeriod; }
    public BigDecimal getInsuredAmount() { return insuredAmount; }
    public BigDecimal getAnnualPremium() { return annualPremium; }
    public BigDecimal getMonthlyPremium() { return monthlyPremium; }
    public String getRateVersion() { return rateVersion; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
}