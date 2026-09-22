package com.insurance.lifepremium.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "calculation_records")
public class CalculationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "payment_period", nullable = false)
    private String paymentPeriod;

    @Column(name = "insured_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal insuredAmount;

    @Column(name = "annual_premium", nullable = false, precision = 15, scale = 2)
    private BigDecimal annualPremium;

    @Column(name = "monthly_premium", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyPremium;

    @Column(name = "rate_version")
    private String rateVersion;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    protected CalculationRecord() {}

    public CalculationRecord(String agentId, String productCode, Integer age,
                              String paymentPeriod, BigDecimal insuredAmount,
                              BigDecimal annualPremium, BigDecimal monthlyPremium,
                              String rateVersion, LocalDateTime calculatedAt) {
        this.agentId = agentId;
        this.productCode = productCode;
        this.age = age;
        this.paymentPeriod = paymentPeriod;
        this.insuredAmount = insuredAmount;
        this.annualPremium = annualPremium;
        this.monthlyPremium = monthlyPremium;
        this.rateVersion = rateVersion;
        this.calculatedAt = calculatedAt;
    }

    public Long getId() { return id; }
    public String getAgentId() { return agentId; }
    public String getProductCode() { return productCode; }
    public Integer getAge() { return age; }
    public String getPaymentPeriod() { return paymentPeriod; }
    public BigDecimal getInsuredAmount() { return insuredAmount; }
    public BigDecimal getAnnualPremium() { return annualPremium; }
    public BigDecimal getMonthlyPremium() { return monthlyPremium; }
    public String getRateVersion() { return rateVersion; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
}