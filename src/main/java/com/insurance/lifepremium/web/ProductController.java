package com.insurance.lifepremium.web;

import com.insurance.lifepremium.application.dto.ApiResponse;
import com.insurance.lifepremium.application.dto.ProductRequest;
import com.insurance.lifepremium.application.service.ProductService;
import com.insurance.lifepremium.domain.model.Product;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> registerProduct(@Valid @RequestBody ProductRequest request) {
        Product product = productService.registerProduct(request.getProductCode(), request.getProductName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product registered: " + product.getProductCode()));
    }

    @GetMapping("/{productCode}")
    public ResponseEntity<ApiResponse<String>> getProduct(@PathVariable String productCode) {
        Product product = productService.getProduct(productCode);
        return ResponseEntity.ok(ApiResponse.ok(product.getProductCode() + " - " + product.getProductName()));
    }
}