package com.insurance.lifepremium.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class RateEntryDto {
    @NotNull
    private Integer age;
    @NotBlank
    private String paymentPeriod;
    @NotNull
    private BigDecimal rate;

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getPaymentPeriod() { return paymentPeriod; }
    public void setPaymentPeriod(String paymentPeriod) { this.paymentPeriod = paymentPeriod; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
}