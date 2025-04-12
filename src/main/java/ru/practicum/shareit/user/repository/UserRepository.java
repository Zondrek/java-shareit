package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {

    long createUser(User user);

    void updateUser(User user);

    Collection<User> getUsers();

    User getUser(long id);

    void deleteUser(long id);
}
