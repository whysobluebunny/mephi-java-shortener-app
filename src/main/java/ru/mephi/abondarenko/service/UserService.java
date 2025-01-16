package ru.mephi.abondarenko.service;

import ru.mephi.abondarenko.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserService {
    private final Map<UUID, User> users = new HashMap<>();

    public User createUser(String username) {
        User user = new User(username);
        users.put(user.getUserId(), user);
        return user;
    }

    public User getUser(UUID userId) {
        return users.get(userId);
    }
}