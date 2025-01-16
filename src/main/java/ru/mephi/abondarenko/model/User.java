package ru.mephi.abondarenko.model;

import lombok.Data;
import java.util.UUID;

@Data
public class User {
    private UUID userId;
    private String username;

    public User(String username) {
        this.userId = UUID.randomUUID();
        this.username = username;
    }
}