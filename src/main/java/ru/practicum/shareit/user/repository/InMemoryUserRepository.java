package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.error.exception.ConflictException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();

    private final Map<String, Long> emails = new HashMap<>();

    @Override
    public long createUser(User user) {
        if (emails.containsKey(user.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует!");
        }
        user.setId(createId());
        users.put(user.getId(), user);
        emails.put(user.getEmail(), user.getId());
        return user.getId();
    }

    @Override
    public void updateUser(User user) {
        User oldUser = users.get(user.getId());
        if (oldUser == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        if (!oldUser.getEmail().equals(user.getEmail())) {
            Long userIdWithEmail = emails.get(user.getEmail());
            if (userIdWithEmail != null && !userIdWithEmail.equals(user.getId())) {
                throw new ConflictException("Данный email уже используется!");
            }
            emails.remove(oldUser.getEmail());
            emails.put(user.getEmail(), user.getId());
        }

        users.put(user.getId(), user);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователя с таким идентификатором не существует");
        }
        return user;
    }

    @Override
    public void deleteUser(long id) {
        User user = users.get(id);
        if (user != null) {
            emails.remove(user.getEmail());
        }
        users.remove(id);
    }

    private long createId() {
        long currentMaxId = getUsers()
                .stream()
                .map(User::getId)
                .max(Long::compareTo)
                .orElse(0L);
        return ++currentMaxId;
    }
}
