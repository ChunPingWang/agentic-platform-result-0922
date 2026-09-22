package com.insurance.lifepremium.web;

import com.insurance.lifepremium.application.dto.ApiResponse;
import com.insurance.lifepremium.application.dto.RateTableRequest;
import com.insurance.lifepremium.application.dto.RateTableVersionRequest;
import com.insurance.lifepremium.application.service.RateTableService;
import com.insurance.lifepremium.domain.model.RateTable;
import com.insurance.lifepremium.domain.model.RateTableVersion;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rate-tables")
public class RateTableController {

    private final RateTableService rateTableService;

    public RateTableController(RateTableService rateTableService) {
        this.rateTableService = rateTableService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> registerRateTable(@Valid @RequestBody RateTableRequest request) {
        RateTable rt = rateTableService.registerRateTable(request.getProductCode());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Rate table registered for product: " + rt.getProductCode()));
    }

    @PostMapping("/{productCode}/versions")
    public ResponseEntity<ApiResponse<String>> addVersion(
            @PathVariable String productCode,
            @Valid @RequestBody RateTableVersionRequest request) {
        RateTableVersion version = rateTableService.addVersion(
                productCode, request.getVersion(), request.getEntries());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Version added: " + version.getVersion()));
    }
}