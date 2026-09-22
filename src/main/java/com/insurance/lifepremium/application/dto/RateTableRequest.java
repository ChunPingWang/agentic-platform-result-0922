package com.insurance.lifepremium.application.dto;

import jakarta.validation.constraints.NotBlank;

public class RateTableRequest {
    @NotBlank
    private String productCode;

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
}