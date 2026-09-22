package com.insurance.lifepremium.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rate_tables")
public class RateTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", nullable = false, unique = true)
    private String productCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "rateTable", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RateTableVersion> versions = new ArrayList<>();

    protected RateTable() {}

    public RateTable(String productCode) {
        this.productCode = productCode;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getProductCode() { return productCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<RateTableVersion> getVersions() { return versions; }
}