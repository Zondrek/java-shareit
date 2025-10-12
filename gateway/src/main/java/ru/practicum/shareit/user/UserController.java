package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.validation.ValidationGroup;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserClient userClient;

    @PostMapping
    @Validated(ValidationGroup.OnCreate.class)
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserRequestDto user) {
        return userClient.createUser(user);
    }

    @PatchMapping("/{id}")
    @Validated(ValidationGroup.OnUpdate.class)
    public ResponseEntity<Object> updateUser(@PathVariable long id, @Valid @RequestBody UserRequestDto user) {
        return userClient.updateUser(id, user);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        return userClient.getUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUser(@PathVariable long id) {
        return userClient.getUser(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable long id) {
        return userClient.deleteUser(id);
    }
}