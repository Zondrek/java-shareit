package ru.practicum.shareit.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.validation.ValidationGroup;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserRequestDtoJsonTest {

    @Autowired
    private JacksonTester<UserRequestDto> json;

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testSerialize() throws Exception {
        // Given
        UserRequestDto dto = UserRequestDto.builder()
                .email("user@example.com")
                .name("Test User")
                .build();

        // When/Then
        assertThat(json.write(dto)).hasJsonPath("$.email");
        assertThat(json.write(dto)).hasJsonPath("$.name");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.email")
                .isEqualTo("user@example.com");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.name")
                .isEqualTo("Test User");
    }

    @Test
    void testDeserialize() throws Exception {
        // Given
        String content = "{\"email\":\"user@example.com\",\"name\":\"Test User\"}";

        // When
        UserRequestDto dto = json.parse(content).getObject();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getEmail()).isEqualTo("user@example.com");
        assertThat(dto.getName()).isEqualTo("Test User");
    }

    @Test
    void testValidation_onCreate_shouldPassWithValidEmail() {
        // Given
        UserRequestDto dto = UserRequestDto.builder()
                .email("user@example.com")
                .name("Test User")
                .build();

        // When
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(dto, ValidationGroup.OnCreate.class);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void testValidation_onCreate_shouldFailWhenEmailIsNull() {
        // Given
        UserRequestDto dto = UserRequestDto.builder()
                .name("Test User")
                .build();

        // When
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(dto, ValidationGroup.OnCreate.class);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void testValidation_shouldFailWhenEmailIsInvalid() {
        // Given
        UserRequestDto dto = UserRequestDto.builder()
                .email("invalid-email")
                .name("Test User")
                .build();

        // When - validate with Default group (where @Email and @Pattern are)
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void testValidation_shouldFailWhenEmailIsBlank() {
        // Given
        UserRequestDto dto = UserRequestDto.builder()
                .email("   ")
                .name("Test User")
                .build();

        // When - validate with Default group (where @Pattern is)
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void testValidation_shouldFailWhenEmailContainsSpaces() {
        // Given
        UserRequestDto dto = UserRequestDto.builder()
                .email("user @example.com")
                .name("Test User")
                .build();

        // When - validate with Default group (where @Pattern is)
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void testValidation_onUpdate_shouldAllowNullEmail() {
        // Given - partial update, email can be null
        UserRequestDto dto = UserRequestDto.builder()
                .name("Test User")
                .build();

        // When - using OnUpdate group
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(dto, ValidationGroup.OnUpdate.class);

        // Then - should pass because @NotNull is only for OnCreate
        assertThat(violations).isEmpty();
    }

    @Test
    void testValidation_onUpdate_shouldValidateEmailFormat_whenPresent() {
        // Given - partial update with invalid email
        UserRequestDto dto = UserRequestDto.builder()
                .email("invalid-email")
                .name("Test User")
                .build();

        // When - validate with Default group (where @Email and @Pattern are)
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(dto);

        // Then - should fail because @Email and @Pattern are in Default group
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void testValidation_shouldAllowNullName() {
        // Given
        UserRequestDto dto = UserRequestDto.builder()
                .email("user@example.com")
                .build();

        // When
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(dto, ValidationGroup.OnCreate.class);

        // Then - name is optional
        assertThat(violations).isEmpty();
    }
}