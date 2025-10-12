package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.model.UserResponseDto;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        // Given
        UserRequestDto requestDto = UserRequestDto.builder()
                .email("user@example.com")
                .name("Test User")
                .build();

        // When
        UserResponseDto created = userService.createUser(requestDto);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getEmail()).isEqualTo("user@example.com");
        assertThat(created.getName()).isEqualTo("Test User");
    }

    @Test
    void updateUser_shouldUpdateUserSuccessfully() {
        // Given
        UserRequestDto createDto = UserRequestDto.builder()
                .email("user@example.com")
                .name("Original Name")
                .build();
        UserResponseDto user = userService.createUser(createDto);

        UserRequestDto updateDto = UserRequestDto.builder()
                .email("updated@example.com")
                .name("Updated Name")
                .build();

        // When
        UserResponseDto updated = userService.updateUser(user.getId(), updateDto);

        // Then
        assertThat(updated.getId()).isEqualTo(user.getId());
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void updateUser_shouldPartiallyUpdateEmail() {
        // Given
        UserRequestDto createDto = UserRequestDto.builder()
                .email("user@example.com")
                .name("Test User")
                .build();
        UserResponseDto user = userService.createUser(createDto);

        UserRequestDto updateDto = UserRequestDto.builder()
                .email("newemail@example.com")
                .build();

        // When
        UserResponseDto updated = userService.updateUser(user.getId(), updateDto);

        // Then
        assertThat(updated.getId()).isEqualTo(user.getId());
        assertThat(updated.getEmail()).isEqualTo("newemail@example.com");
        assertThat(updated.getName()).isEqualTo("Test User"); // Name unchanged
    }

    @Test
    void updateUser_shouldPartiallyUpdateName() {
        // Given
        UserRequestDto createDto = UserRequestDto.builder()
                .email("user@example.com")
                .name("Original Name")
                .build();
        UserResponseDto user = userService.createUser(createDto);

        UserRequestDto updateDto = UserRequestDto.builder()
                .name("New Name")
                .build();

        // When
        UserResponseDto updated = userService.updateUser(user.getId(), updateDto);

        // Then
        assertThat(updated.getId()).isEqualTo(user.getId());
        assertThat(updated.getEmail()).isEqualTo("user@example.com"); // Email unchanged
        assertThat(updated.getName()).isEqualTo("New Name");
    }

    @Test
    void updateUser_shouldThrowNotFoundException_whenUserNotExists() {
        // Given
        UserRequestDto updateDto = UserRequestDto.builder()
                .email("updated@example.com")
                .name("Updated Name")
                .build();

        // When/Then
        assertThatThrownBy(() -> userService.updateUser(999L, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void getUsers_shouldReturnAllUsers() {
        // Given
        userService.createUser(UserRequestDto.builder()
                .email("user1@example.com")
                .name("User 1")
                .build());
        userService.createUser(UserRequestDto.builder()
                .email("user2@example.com")
                .name("User 2")
                .build());
        userService.createUser(UserRequestDto.builder()
                .email("user3@example.com")
                .name("User 3")
                .build());

        // When
        Collection<UserResponseDto> users = userService.getUsers();

        // Then
        assertThat(users).hasSize(3);
        assertThat(users).extracting(UserResponseDto::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    void getUsers_shouldReturnEmptyList_whenNoUsers() {
        // When
        Collection<UserResponseDto> users = userService.getUsers();

        // Then
        assertThat(users).isEmpty();
    }

    @Test
    void getUser_shouldReturnUser() {
        // Given
        UserRequestDto createDto = UserRequestDto.builder()
                .email("user@example.com")
                .name("Test User")
                .build();
        UserResponseDto created = userService.createUser(createDto);

        // When
        UserResponseDto found = userService.getUser(created.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getEmail()).isEqualTo("user@example.com");
        assertThat(found.getName()).isEqualTo("Test User");
    }

    @Test
    void getUser_shouldThrowNotFoundException_whenUserNotExists() {
        // When/Then
        assertThatThrownBy(() -> userService.getUser(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void deleteUser_shouldDeleteUserSuccessfully() {
        // Given
        UserRequestDto createDto = UserRequestDto.builder()
                .email("user@example.com")
                .name("Test User")
                .build();
        UserResponseDto created = userService.createUser(createDto);

        // When
        userService.deleteUser(created.getId());

        // Then
        assertThatThrownBy(() -> userService.getUser(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteUser_shouldNotThrow_whenUserNotExists() {
        // When/Then - deleteById doesn't throw in Spring Data JPA
        userService.deleteUser(999L);
    }
}