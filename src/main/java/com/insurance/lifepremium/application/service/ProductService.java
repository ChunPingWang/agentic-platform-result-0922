package com.insurance.lifepremium.application.service;

import com.insurance.lifepremium.domain.exception.DomainException;
import com.insurance.lifepremium.domain.model.Product;
import com.insurance.lifepremium.infrastructure.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product registerProduct(String productCode, String productName) {
        if (productRepository.existsByProductCode(productCode)) {
            throw new DomainException("DUPLICATE_PRODUCT", "Product already registered: " + productCode);
        }
        return productRepository.save(new Product(productCode, productName));
    }

    @Transactional(readOnly = true)
    public Product getProduct(String productCode) {
        return productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new DomainException("PRODUCT_NOT_FOUND", "Product not found: " + productCode));
    }
}