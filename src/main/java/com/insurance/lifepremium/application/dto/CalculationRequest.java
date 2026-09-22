package com.insurance.lifepremium.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CalculationRequest {

    @NotBlank
    private String productCode;

    @NotNull
    private Integer age;

    @NotBlank
    private String paymentPeriod;

    @NotNull
    private BigDecimal insuredAmount;

    private String rateVersion;

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getPaymentPeriod() { return paymentPeriod; }
    public void setPaymentPeriod(String paymentPeriod) { this.paymentPeriod = paymentPeriod; }
    public BigDecimal getInsuredAmount() { return insuredAmount; }
    public void setInsuredAmount(BigDecimal insuredAmount) { this.insuredAmount = insuredAmount; }
    public String getRateVersion() { return rateVersion; }
    public void setRateVersion(String rateVersion) { this.rateVersion = rateVersion; }
}