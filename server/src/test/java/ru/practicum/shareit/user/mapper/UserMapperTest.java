package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserResponseDto;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toUser_shouldMapFromRequestDto() {
        // Given
        UserRequestDto dto = UserRequestDto.builder()
                .email("user@test.com")
                .name("Test User")
                .build();

        // When
        User user = UserMapper.toUser(dto);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("user@test.com");
        assertThat(user.getName()).isEqualTo("Test User");
        assertThat(user.getId()).isNull();
    }

    @Test
    void toUser_shouldMapFromUserDto() {
        // Given
        UserDto dto = UserDto.builder()
                .id(1L)
                .email("user@test.com")
                .name("Test User")
                .build();

        // When
        User user = UserMapper.toUser(dto);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("user@test.com");
        assertThat(user.getName()).isEqualTo("Test User");
    }

    @Test
    void toUserResponseDto_shouldMapFromUser() {
        // Given
        User user = User.builder()
                .id(1L)
                .email("user@test.com")
                .name("Test User")
                .build();

        // When
        UserResponseDto dto = UserMapper.toUserResponseDto(user);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEmail()).isEqualTo("user@test.com");
        assertThat(dto.getName()).isEqualTo("Test User");
    }

    @Test
    void toUserDto_shouldMapFromUser() {
        // Given
        User user = User.builder()
                .id(1L)
                .email("user@test.com")
                .name("Test User")
                .build();

        // When
        UserDto dto = UserMapper.toUserDto(user);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEmail()).isEqualTo("user@test.com");
        assertThat(dto.getName()).isEqualTo("Test User");
    }

    @Test
    void toUserResponseDtoCollection_shouldMapCollection() {
        // Given
        List<User> users = List.of(
                User.builder().id(1L).email("user1@test.com").name("User 1").build(),
                User.builder().id(2L).email("user2@test.com").name("User 2").build(),
                User.builder().id(3L).email("user3@test.com").name("User 3").build()
        );

        // When
        Collection<UserResponseDto> dtos = UserMapper.toUserResponseDtoCollection(users);

        // Then
        assertThat(dtos).hasSize(3);
        assertThat(dtos).extracting(UserResponseDto::getId).containsExactly(1L, 2L, 3L);
        assertThat(dtos).extracting(UserResponseDto::getEmail)
                .containsExactly("user1@test.com", "user2@test.com", "user3@test.com");
    }

    @Test
    void toUserResponseDtoCollection_shouldReturnEmptyCollection_whenInputEmpty() {
        // Given
        List<User> users = List.of();

        // When
        Collection<UserResponseDto> dtos = UserMapper.toUserResponseDtoCollection(users);

        // Then
        assertThat(dtos).isEmpty();
    }
}