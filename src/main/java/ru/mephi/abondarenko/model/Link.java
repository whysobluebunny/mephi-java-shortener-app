package ru.mephi.abondarenko.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Link {
    private String shortUrl;
    private String originalUrl;
    private LocalDateTime creationTime;
    private LocalDateTime expirationTime;
    private int clickLimit;
    private int clickCount;
    private UUID userId;

    public Link(String originalUrl, int clickLimit, UUID userId) {
        this.originalUrl = originalUrl;
        this.clickLimit = clickLimit;
        this.userId = userId;
        this.creationTime = LocalDateTime.now();
        this.expirationTime = creationTime.plusDays(1);
        this.clickCount = 0;
    }
}