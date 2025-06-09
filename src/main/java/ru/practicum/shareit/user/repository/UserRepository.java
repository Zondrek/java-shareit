package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {

    long createUser(User user);

    void updateUser(User user);

    Collection<User> getUsers();

    Optional<User> getUser(long id);

    void deleteUser(long id);
}
