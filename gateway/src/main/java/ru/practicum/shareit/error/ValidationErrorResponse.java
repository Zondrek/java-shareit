package ru.practicum.shareit.error;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ValidationErrorResponse {
    private String error;
    private List<Violation> violations = new ArrayList<>();

    public ValidationErrorResponse(String error) {
        this.error = error;
    }

    public void addViolation(String field, String message) {
        violations.add(new Violation(field, message));
    }

    @Data
    public static class Violation {
        private final String field;
        private final String message;
    }
}