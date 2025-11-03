package ru.practicum.shareit.request.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testSerialize() throws Exception {
        // Given
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need a drill for home repairs")
                .build();

        // When/Then
        assertThat(json.write(dto)).hasJsonPath("$.description");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need a drill for home repairs");
    }

    @Test
    void testDeserialize() throws Exception {
        // Given
        String content = "{\"description\":\"Need a drill for home repairs\"}";

        // When
        ItemRequestDto dto = json.parse(content).getObject();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getDescription()).isEqualTo("Need a drill for home repairs");
    }

    @Test
    void testValidation_shouldPassWithValidDescription() {
        // Given
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        // When
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void testValidation_shouldFailWhenDescriptionIsNull() {
        // Given
        ItemRequestDto dto = ItemRequestDto.builder()
                .description(null)
                .build();

        // When
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Description cannot be blank");
    }

    @Test
    void testValidation_shouldFailWhenDescriptionIsEmpty() {
        // Given
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("")
                .build();

        // When
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Description cannot be blank");
    }

    @Test
    void testValidation_shouldFailWhenDescriptionIsBlank() {
        // Given
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("   ")
                .build();

        // When
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Description cannot be blank");
    }

    @Test
    void testValidation_shouldFailWhenDescriptionExceedsMaxLength() {
        // Given
        String longDescription = "a".repeat(1001); // 1001 символ (превышает лимит 1000)
        ItemRequestDto dto = ItemRequestDto.builder()
                .description(longDescription)
                .build();

        // When
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Description cannot exceed 1000 characters");
    }

    @Test
    void testValidation_shouldPassWithMaxLengthDescription() {
        // Given
        String maxDescription = "a".repeat(1000); // Ровно 1000 символов
        ItemRequestDto dto = ItemRequestDto.builder()
                .description(maxDescription)
                .build();

        // When
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }
}