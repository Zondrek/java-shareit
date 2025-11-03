package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserResponseDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository repository;

    public UserResponseDto createUser(UserRequestDto dto) {
        User user = UserMapper.toUser(dto);
        User savedUser = repository.save(user);
        return UserMapper.toUserResponseDto(savedUser);
    }

    public UserResponseDto updateUser(long id, UserRequestDto dto) {
        User oldUser = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден, userId = " + id));
        User newUser = UserMapper.toUser(dto);
        newUser.setId(id);
        User result = update(oldUser, newUser);
        User savedUser = repository.save(result);
        return UserMapper.toUserResponseDto(savedUser);
    }

    public Collection<UserResponseDto> getUsers() {
        return UserMapper.toUserResponseDtoCollection(repository.findAll());
    }

    public UserResponseDto getUser(long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден, userId = " + id));
        return UserMapper.toUserResponseDto(user);
    }

    public void deleteUser(long id) {
        repository.deleteById(id);
    }

    private User update(User originalUser, User newUser) {
        return User.builder()
                .id(newUser.getId())
                .name(newUser.getName() == null ? originalUser.getName() : newUser.getName())
                .email(newUser.getEmail() == null ? originalUser.getEmail() : newUser.getEmail())
                .build();
    }
}