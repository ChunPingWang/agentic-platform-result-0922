package com.insurance.lifepremium.infrastructure.repository;

import com.insurance.lifepremium.domain.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByAgentId(String agentId);
}