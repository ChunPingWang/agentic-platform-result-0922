package com.insurance.lifepremium.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "rate_entries",
       uniqueConstraints = @UniqueConstraint(columnNames = {"rate_table_version_id", "age", "payment_period"}))
public class RateEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rate_table_version_id", nullable = false)
    private RateTableVersion rateTableVersion;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "payment_period", nullable = false)
    private String paymentPeriod;

    @Column(name = "rate", nullable = false, precision = 10, scale = 6)
    private BigDecimal rate;

    protected RateEntry() {}

    public RateEntry(RateTableVersion rateTableVersion, Integer age, String paymentPeriod, BigDecimal rate) {
        this.rateTableVersion = rateTableVersion;
        this.age = age;
        this.paymentPeriod = paymentPeriod;
        this.rate = rate;
    }

    public Long getId() { return id; }
    public RateTableVersion getRateTableVersion() { return rateTableVersion; }
    public Integer getAge() { return age; }
    public String getPaymentPeriod() { return paymentPeriod; }
    public BigDecimal getRate() { return rate; }
}