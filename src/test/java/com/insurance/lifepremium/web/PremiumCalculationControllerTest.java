package com.insurance.lifepremium.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.lifepremium.application.dto.RateEntryDto;
import com.insurance.lifepremium.application.service.ProductService;
import com.insurance.lifepremium.application.service.RateTableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class PremiumCalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private RateTableService rateTableService;

    private static final String PRODUCT_CODE = "LIFE-WL-CTRL";

    @BeforeEach
    void setUp() {
        productService.registerProduct(PRODUCT_CODE, "Controller Test Product");
        rateTableService.registerRateTable(PRODUCT_CODE);
        RateEntryDto entry = new RateEntryDto();
        entry.setAge(30);
        entry.setPaymentPeriod("ANNUAL");
        entry.setRate(new BigDecimal("0.005000"));
        rateTableService.addVersion(PRODUCT_CODE, "v1", List.of(entry));
    }

    @Test
    void calculate_validRequest_returns201() throws Exception {
        Map<String, Object> body = Map.of(
                "productCode", PRODUCT_CODE,
                "age", 30,
                "paymentPeriod", "ANNUAL",
                "insuredAmount", 1000000,
                "rateVersion", "v1"
        );

        mockMvc.perform(post("/api/v1/calculations")
                        .header("X-Agent-Id", "agent-ctrl-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.annualPremium").exists())
                .andExpect(jsonPath("$.data.monthlyPremium").exists());
    }

    // TC-CALC-021: missing required fields
    @Test
    void calculate_missingRequiredFields_returns400() throws Exception {
        Map<String, Object> body = Map.of("age", 30);

        mockMvc.perform(post("/api/v1/calculations")
                        .header("X-Agent-Id", "agent-ctrl-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculate_invalidAge_returns422() throws Exception {
        Map<String, Object> body = Map.of(
                "productCode", PRODUCT_CODE,
                "age", -1,
                "paymentPeriod", "ANNUAL",
                "insuredAmount", 1000000,
                "rateVersion", "v1"
        );

        mockMvc.perform(post("/api/v1/calculations")
                        .header("X-Agent-Id", "agent-ctrl-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void queryHistory_returnsOk() throws Exception {
        // First create a record
        Map<String, Object> body = Map.of(
                "productCode", PRODUCT_CODE,
                "age", 30,
                "paymentPeriod", "ANNUAL",
                "insuredAmount", 1000000,
                "rateVersion", "v1"
        );
        mockMvc.perform(post("/api/v1/calculations")
                        .header("X-Agent-Id", "agent-ctrl-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)));

        mockMvc.perform(get("/api/v1/calculations/history")
                        .header("X-Agent-Id", "agent-ctrl-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}