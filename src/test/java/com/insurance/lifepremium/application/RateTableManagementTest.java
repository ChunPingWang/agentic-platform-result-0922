package com.insurance.lifepremium.application;

import com.insurance.lifepremium.application.dto.RateEntryDto;
import com.insurance.lifepremium.application.service.ProductService;
import com.insurance.lifepremium.application.service.RateTableService;
import com.insurance.lifepremium.domain.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RateTableManagementTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private RateTableService rateTableService;

    private static final String PRODUCT_CODE = "LIFE-RATE-TEST";

    @BeforeEach
    void setUp() {
        productService.registerProduct(PRODUCT_CODE, "Rate Test Product");
    }

    // TC-RATE-001: register product
    @Test
    void registerProduct_success() {
        String code = "NEW-PRODUCT-001";
        productService.registerProduct(code, "New Product");
        assertThatNoException().isThrownBy(() -> productService.getProduct(code));
    }

    // TC-RATE-002: register rate table
    @Test
    void registerRateTable_success() {
        var rt = rateTableService.registerRateTable(PRODUCT_CODE);
        assertThat(rt.getProductCode()).isEqualTo(PRODUCT_CODE);
    }

    // TC-RATE-003: add version with entries
    @Test
    void addVersion_success() {
        rateTableService.registerRateTable(PRODUCT_CODE);
        RateEntryDto entry = buildEntry(30, "ANNUAL", "0.005");
        var version = rateTableService.addVersion(PRODUCT_CODE, "v1", List.of(entry));
        assertThat(version.getVersion()).isEqualTo("v1");
    }

    // TC-RATE-004: rate can be retrieved after adding version
    @Test
    void findRate_afterAddingVersion_returnsRate() {
        rateTableService.registerRateTable(PRODUCT_CODE);
        RateEntryDto entry = buildEntry(30, "ANNUAL", "0.005");
        rateTableService.addVersion(PRODUCT_CODE, "v1", List.of(entry));

        Optional<BigDecimal> rate = rateTableService.findRate(PRODUCT_CODE, "v1", 30, "ANNUAL");
        assertThat(rate).isPresent();
        assertThat(rate.get()).isEqualByComparingTo("0.005");
    }

    // TC-RATE-005: duplicate product
    @Test
    void registerProduct_duplicate_throwsDomainException() {
        assertThatThrownBy(() -> productService.registerProduct(PRODUCT_CODE, "Duplicate"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo("DUPLICATE_PRODUCT"));
    }

    // TC-RATE-006: rate table for unregistered product
    @Test
    void registerRateTable_productNotFound_throwsDomainException() {
        assertThatThrownBy(() -> rateTableService.registerRateTable("UNKNOWN-PRODUCT"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo("PRODUCT_NOT_FOUND"));
    }

    // TC-RATE-007: duplicate rate table
    @Test
    void registerRateTable_duplicate_throwsDomainException() {
        rateTableService.registerRateTable(PRODUCT_CODE);
        assertThatThrownBy(() -> rateTableService.registerRateTable(PRODUCT_CODE))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo("DUPLICATE_RATE_TABLE"));
    }

    // TC-RATE-008: add version to non-existent rate table
    @Test
    void addVersion_rateTableNotFound_throwsDomainException() {
        assertThatThrownBy(() -> rateTableService.addVersion("NO-TABLE", "v1", List.of()))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo("RATE_TABLE_NOT_FOUND"));
    }

    // TC-RATE-009: duplicate version
    @Test
    void addVersion_duplicate_throwsDomainException() {
        rateTableService.registerRateTable(PRODUCT_CODE);
        rateTableService.addVersion(PRODUCT_CODE, "v1", List.of());
        assertThatThrownBy(() -> rateTableService.addVersion(PRODUCT_CODE, "v1", List.of()))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo("DUPLICATE_VERSION"));
    }

    // TC-RATE-011: version at boundary (v99)
    @Test
    void addVersion_boundaryVersion_success() {
        rateTableService.registerRateTable(PRODUCT_CODE);
        var version = rateTableService.addVersion(PRODUCT_CODE, "v99", List.of());
        assertThat(version.getVersion()).isEqualTo("v99");
    }

    // TC-RATE-012: rate entry boundary values
    @Test
    void addVersion_boundaryRateEntries_success() {
        rateTableService.registerRateTable(PRODUCT_CODE);
        RateEntryDto minEntry = buildEntry(0, "ANNUAL", "0.000001");
        RateEntryDto maxEntry = buildEntry(100, "MONTHLY", "9.999999");
        var version = rateTableService.addVersion(PRODUCT_CODE, "v1", List.of(minEntry, maxEntry));
        assertThat(version).isNotNull();
    }

    // TC-RATE-013: duplicate rate entry key
    @Test
    void addVersion_duplicateRateEntry_throwsDomainException() {
        rateTableService.registerRateTable(PRODUCT_CODE);
        RateEntryDto e1 = buildEntry(30, "ANNUAL", "0.005");
        RateEntryDto e2 = buildEntry(30, "ANNUAL", "0.006");
        assertThatThrownBy(() -> rateTableService.addVersion(PRODUCT_CODE, "v1", List.of(e1, e2)))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo("DUPLICATE_RATE_ENTRY"));
    }

    // TC-RATE-014: rate table registered, cache state (no-op cache in test)
    @Test
    void registerRateTable_cacheState_noError() {
        rateTableService.registerRateTable(PRODUCT_CODE);
        RateEntryDto entry = buildEntry(30, "ANNUAL", "0.005");
        rateTableService.addVersion(PRODUCT_CODE, "v1", List.of(entry));
        Optional<BigDecimal> rate = rateTableService.findRate(PRODUCT_CODE, "v1", 30, "ANNUAL");
        assertThat(rate).isPresent();
    }

    // TC-RATE-015: new version does not invalidate old version
    @Test
    void addVersion_newVersionDoesNotInvalidateOld() {
        rateTableService.registerRateTable(PRODUCT_CODE);
        RateEntryDto e1 = buildEntry(30, "ANNUAL", "0.005");
        RateEntryDto e2 = buildEntry(30, "ANNUAL", "0.006");
        rateTableService.addVersion(PRODUCT_CODE, "v1", List.of(e1));
        rateTableService.addVersion(PRODUCT_CODE, "v2", List.of(e2));

        Optional<BigDecimal> rateV1 = rateTableService.findRate(PRODUCT_CODE, "v1", 30, "ANNUAL");
        Optional<BigDecimal> rateV2 = rateTableService.findRate(PRODUCT_CODE, "v2", 30, "ANNUAL");

        assertThat(rateV1).isPresent();
        assertThat(rateV2).isPresent();
        assertThat(rateV1.get()).isEqualByComparingTo("0.005");
        assertThat(rateV2.get()).isEqualByComparingTo("0.006");
    }

    // TC-RATE-016: empty rate entries list
    @Test
    void addVersion_emptyEntries_success() {
        rateTableService.registerRateTable(PRODUCT_CODE);
        var version = rateTableService.addVersion(PRODUCT_CODE, "v1", List.of());
        assertThat(version.getVersion()).isEqualTo("v1");
    }

    private RateEntryDto buildEntry(int age, String period, String rate) {
        RateEntryDto dto = new RateEntryDto();
        dto.setAge(age);
        dto.setPaymentPeriod(period);
        dto.setRate(new BigDecimal(rate));
        return dto;
    }
}