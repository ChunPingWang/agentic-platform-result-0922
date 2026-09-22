package com.insurance.lifepremium.domain.exception;

import java.util.List;

public class ValidationException extends DomainException {
    private final List<String> violations;

    public ValidationException(List<String> violations) {
        super("VALIDATION_ERROR", "Input validation failed: " + violations);
        this.violations = violations;
    }

    public List<String> getViolations() { return violations; }
}