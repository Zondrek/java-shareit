package ru.practicum.shareit.error.dto;

import java.util.List;

public record ValidationErrorResponse(List<Violation> violations) {
}