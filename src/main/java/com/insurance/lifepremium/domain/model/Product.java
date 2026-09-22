package com.insurance.lifepremium.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", nullable = false, unique = true)
    private String productCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Product() {}

    public Product(String productCode, String productName) {
        this.productCode = productCode;
        this.productName = productName;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}