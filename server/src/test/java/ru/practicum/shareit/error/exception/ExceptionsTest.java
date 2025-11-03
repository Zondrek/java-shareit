package ru.practicum.shareit.error.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionsTest {

    @Test
    void conflictException_shouldCreateWithMessage() {
        // Given
        String message = "Conflict occurred";

        // When
        ConflictException exception = new ConflictException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void notFoundException_shouldCreateWithMessage() {
        // Given
        String message = "Resource not found";

        // When
        NotFoundException exception = new NotFoundException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void forbiddenException_shouldCreateWithMessage() {
        // Given
        String message = "Access forbidden";

        // When
        ForbiddenException exception = new ForbiddenException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void internalServerException_shouldCreateWithMessage() {
        // Given
        String message = "Internal server error";

        // When
        InternalServerException exception = new InternalServerException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}