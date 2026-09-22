package com.insurance.lifepremium.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rate_table_versions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"rate_table_id", "version"}))
public class RateTableVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rate_table_id", nullable = false)
    private RateTable rateTable;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "rateTableVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RateEntry> rateEntries = new ArrayList<>();

    protected RateTableVersion() {}

    public RateTableVersion(RateTable rateTable, String version) {
        this.rateTable = rateTable;
        this.version = version;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public RateTable getRateTable() { return rateTable; }
    public String getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<RateEntry> getRateEntries() { return rateEntries; }
}