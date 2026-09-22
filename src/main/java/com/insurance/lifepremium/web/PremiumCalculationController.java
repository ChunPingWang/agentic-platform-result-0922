package com.insurance.lifepremium.web;

import com.insurance.lifepremium.application.dto.ApiResponse;
import com.insurance.lifepremium.application.dto.CalculationRequest;
import com.insurance.lifepremium.application.dto.CalculationResponse;
import com.insurance.lifepremium.application.service.PremiumCalculationService;
import com.insurance.lifepremium.domain.model.CalculationRecord;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/calculations")
@Profile("test")
public class PremiumCalculationController {

    private final PremiumCalculationService calculationService;

    public PremiumCalculationController(PremiumCalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CalculationResponse>> calculate(
            @Valid @RequestBody CalculationRequest request,
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = "X-Agent-Id", required = false) String agentIdHeader) {
        String agentId = resolveAgentId(principal, agentIdHeader);
        CalculationResponse response = calculationService.calculate(agentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<CalculationRecord>>> queryHistory(
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = "X-Agent-Id", required = false) String agentIdHeader,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String agentId = resolveAgentId(principal, agentIdHeader);
        Page<CalculationRecord> records = calculationService.queryHistory(agentId, from, to, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(records));
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<ApiResponse<CalculationRecord>> getRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = "X-Agent-Id", required = false) String agentIdHeader) {
        String agentId = resolveAgentId(principal, agentIdHeader);
        CalculationRecord record = calculationService.getRecord(recordId, agentId);
        return ResponseEntity.ok(ApiResponse.ok(record));
    }

    private String resolveAgentId(UserDetails principal, String header) {
        if (principal != null) return principal.getUsername();
        if (header != null && !header.isBlank()) return header;
        return "anonymous";
    }
}