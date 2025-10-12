package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserRequestDto;

@Slf4j
@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> createUser(UserRequestDto user) {
        return post(API_PREFIX, user);
    }

    public ResponseEntity<Object> updateUser(long id, UserRequestDto user) {
        return patch(API_PREFIX + "/" + id, user);
    }

    public ResponseEntity<Object> getUsers() {
        return get(API_PREFIX);
    }

    public ResponseEntity<Object> getUser(long id) {
        return get(API_PREFIX + "/" + id);
    }

    public ResponseEntity<Object> deleteUser(long id) {
        return delete(API_PREFIX + "/" + id);
    }
}