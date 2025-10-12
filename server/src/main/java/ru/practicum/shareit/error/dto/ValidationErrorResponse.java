package ru.practicum.shareit.error.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ValidationErrorResponse(
        @JsonProperty("violations") List<Violation> violations,
        @JsonProperty("error") String error
) {
    public ValidationErrorResponse(List<Violation> violations) {
        this(violations, "Validation failed");
    }
}