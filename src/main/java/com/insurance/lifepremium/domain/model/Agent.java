package com.insurance.lifepremium.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "agents")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false, unique = true)
    private String agentId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    protected Agent() {}

    public Agent(String agentId, String passwordHash) {
        this.agentId = agentId;
        this.passwordHash = passwordHash;
        this.enabled = true;
    }

    public Long getId() { return id; }
    public String getAgentId() { return agentId; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isEnabled() { return enabled; }
}