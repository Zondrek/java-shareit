package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.model.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        // Given
        UserRequestDto requestDto = UserRequestDto.builder()
                .email("user@example.com")
                .name("Test User")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .email("user@example.com")
                .name("Test User")
                .build();

        when(userService.createUser(any(UserRequestDto.class)))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("user@example.com")))
                .andExpect(jsonPath("$.name", is("Test User")));
    }

    @Test
    void createUser_shouldReturn400_whenEmailIsNull() throws Exception {
        // Given
        UserRequestDto requestDto = UserRequestDto.builder()
                .name("Test User")
                .build();

        // When/Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturn400_whenEmailIsInvalid() throws Exception {
        // Given
        UserRequestDto requestDto = UserRequestDto.builder()
                .email("invalid-email")
                .name("Test User")
                .build();

        // When/Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturn400_whenEmailIsBlank() throws Exception {
        // Given
        UserRequestDto requestDto = UserRequestDto.builder()
                .email("   ")
                .name("Test User")
                .build();

        // When/Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        // Given
        UserRequestDto requestDto = UserRequestDto.builder()
                .email("updated@example.com")
                .name("Updated Name")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .email("updated@example.com")
                .name("Updated Name")
                .build();

        when(userService.updateUser(eq(1L), any(UserRequestDto.class)))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.name", is("Updated Name")));
    }

    @Test
    void updateUser_shouldReturn404_whenUserNotExists() throws Exception {
        // Given
        UserRequestDto requestDto = UserRequestDto.builder()
                .email("updated@example.com")
                .name("Updated Name")
                .build();

        when(userService.updateUser(eq(999L), any(UserRequestDto.class)))
                .thenThrow(new NotFoundException("Пользователь не найден, userId = 999"));

        // When/Then
        mockMvc.perform(patch("/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_shouldAllowPartialUpdate() throws Exception {
        // Given - only name, no email
        UserRequestDto requestDto = UserRequestDto.builder()
                .name("Updated Name Only")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .email("original@example.com")
                .name("Updated Name Only")
                .build();

        when(userService.updateUser(eq(1L), any(UserRequestDto.class)))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("original@example.com")))
                .andExpect(jsonPath("$.name", is("Updated Name Only")));
    }

    @Test
    void getUsers_shouldReturnAllUsers() throws Exception {
        // Given
        UserResponseDto user1 = UserResponseDto.builder()
                .id(1L)
                .email("user1@example.com")
                .name("User 1")
                .build();

        UserResponseDto user2 = UserResponseDto.builder()
                .id(2L)
                .email("user2@example.com")
                .name("User 2")
                .build();

        when(userService.getUsers())
                .thenReturn(List.of(user1, user2));

        // When/Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].email", is("user1@example.com")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].email", is("user2@example.com")));
    }

    @Test
    void getUsers_shouldReturnEmptyList_whenNoUsers() throws Exception {
        // Given
        when(userService.getUsers())
                .thenReturn(List.of());

        // When/Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUser_shouldReturnUser() throws Exception {
        // Given
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .email("user@example.com")
                .name("Test User")
                .build();

        when(userService.getUser(1L))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("user@example.com")))
                .andExpect(jsonPath("$.name", is("Test User")));
    }

    @Test
    void getUser_shouldReturn404_whenUserNotExists() throws Exception {
        // Given
        when(userService.getUser(999L))
                .thenThrow(new NotFoundException("Пользователь не найден, userId = 999"));

        // When/Then
        mockMvc.perform(get("/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_shouldDeleteUser() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When/Then
        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1L);
    }
}