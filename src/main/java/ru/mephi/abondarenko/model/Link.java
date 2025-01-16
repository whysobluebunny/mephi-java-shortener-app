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
    private int lifetimeHours; // Время жизни ссылки в часах

    public Link(String originalUrl, int clickLimit, int lifetimeHours, UUID userId) {
        this.originalUrl = originalUrl;
        this.clickLimit = clickLimit;
        this.lifetimeHours = lifetimeHours;
        this.userId = userId;
        this.creationTime = LocalDateTime.now();
        this.expirationTime = creationTime.plusHours(lifetimeHours); // Расчет времени истечения
        this.clickCount = 0;
    }

    // Метод для проверки, активна ли ссылка
    public boolean isActive() {
        return clickCount < clickLimit && expirationTime.isAfter(LocalDateTime.now());
    }
}