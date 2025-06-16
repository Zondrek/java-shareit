package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.model.UserRequestDto;
import ru.practicum.shareit.user.model.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.ValidationGroup;

import java.util.Collection;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService service;

    @PostMapping
    @Validated(ValidationGroup.OnCreate.class)
    public UserResponseDto createUser(@Valid @RequestBody UserRequestDto user) {
        return service.createUser(user);
    }

    @PatchMapping("/{id}")
    @Validated(ValidationGroup.OnUpdate.class)
    public UserResponseDto updateUser(@PathVariable long id, @Valid @RequestBody UserRequestDto user) {
        return service.updateUser(id, user);
    }

    @GetMapping
    public Collection<UserResponseDto> getUsers() {
        return service.getUsers();
    }

    @GetMapping("/{id}")
    public UserResponseDto getUser(@PathVariable long id) {
        return service.getUser(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {
        service.deleteUser(id);
    }
}
