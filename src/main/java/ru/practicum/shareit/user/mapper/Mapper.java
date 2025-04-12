package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserRequestDto;
import ru.practicum.shareit.user.model.UserResponseDto;

import java.util.Collection;

public class Mapper {

    public static UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(UserRequestDto userDto) {
        return User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public static Collection<UserResponseDto> toDtoCollection(Collection<User> userCollection) {
        return userCollection.stream().map(Mapper::toDto).toList();
    }

    public static Collection<User> toUserCollection(Collection<UserRequestDto> dtoCollection) {
        return dtoCollection.stream().map(Mapper::toUser).toList();
    }
}
