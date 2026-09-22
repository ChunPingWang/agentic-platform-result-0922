package com.insurance.lifepremium.application.dto;

import jakarta.validation.constraints.NotBlank;

public class ProductRequest {
    @NotBlank
    private String productCode;
    @NotBlank
    private String productName;

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}