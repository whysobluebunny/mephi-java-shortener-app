package ru.mephi.abondarenko;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.abondarenko.config.Config;
import ru.mephi.abondarenko.exception.LinkExpiredException;
import ru.mephi.abondarenko.exception.LinkNotFoundException;
import ru.mephi.abondarenko.model.Link;
import ru.mephi.abondarenko.service.LinkService;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkServiceTest {
    private LinkService linkService;
    private UUID userId1;
    private UUID userId2;

    @BeforeEach
    void setUp() {
        linkService = new LinkService();
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
    }

    @Test
    void testCreateShortLink_UniqueForDifferentUsers() {
        String originalUrl = "https://example.com";
        String shortUrl1 = linkService.createShortLink(originalUrl, 5, 24, userId1);
        String shortUrl2 = linkService.createShortLink(originalUrl, 5, 24, userId2);

        assertNotEquals(shortUrl1, shortUrl2, "Ссылки должны быть уникальными для разных пользователей");
    }

    @Test
    void testRedirect_WithinClickLimit() {
        String originalUrl = "https://example.com";
        String shortUrl = linkService.createShortLink(originalUrl, 2, 24, userId1);

        assertEquals(originalUrl, linkService.redirect(shortUrl), "Первый переход должен быть успешным");
        assertEquals(originalUrl, linkService.redirect(shortUrl), "Второй переход должен быть успешным");

        assertThrows(LinkExpiredException.class, () -> linkService.redirect(shortUrl),
                "Третий переход должен выбросить исключение (лимит исчерпан)");
    }

    @Test
    void testRedirect_LinkExpired() {
        String originalUrl = "https://example.com";
        String shortUrl = linkService.createShortLink(originalUrl, 5, 1, userId1); // Время жизни — 1 час

        // Устанавливаем время жизни ссылки в прошлое
        Link link = linkService.getLink(shortUrl);
        link.setExpirationTime(LocalDateTime.now().minusHours(1));

        assertThrows(LinkExpiredException.class, () -> linkService.redirect(shortUrl),
                "Переход по просроченной ссылке должен выбросить исключение");
    }

    @Test
    void testRedirect_LinkNotFound() {
        String nonExistentShortUrl = "clck.ru/123456";

        assertThrows(LinkNotFoundException.class, () -> linkService.redirect(nonExistentShortUrl),
                "Переход по несуществующей ссылке должен выбросить исключение");
    }

    @Test
    void testRedirect_AfterExpiration() {
        String originalUrl = "https://example.com";
        String shortUrl = linkService.createShortLink(originalUrl, 5, 1, userId1); // Время жизни — 1 час

        // Устанавливаем время жизни ссылки в прошлое
        Link link = linkService.getLink(shortUrl);
        link.setExpirationTime(LocalDateTime.now().minusHours(1));

        assertThrows(LinkExpiredException.class, () -> linkService.redirect(shortUrl),
                "Переход по ссылке с истекшим сроком должен выбросить исключение");
    }

    @Test
    void testMultipleUsers() {
        String originalUrl = "https://example.com";
        String shortUrl1 = linkService.createShortLink(originalUrl, 5, 24, userId1);
        String shortUrl2 = linkService.createShortLink(originalUrl, 5, 24, userId2);

        assertNotEquals(shortUrl1, shortUrl2, "Ссылки должны быть уникальными для разных пользователей");

        assertEquals(originalUrl, linkService.redirect(shortUrl1), "Первый пользователь должен иметь доступ к своей ссылке");
        assertEquals(originalUrl, linkService.redirect(shortUrl2), "Второй пользователь должен иметь доступ к своей ссылке");
    }

    @Test
    void testUpdateClickLimit() {
        String originalUrl = "https://example.com";
        String shortUrl = linkService.createShortLink(originalUrl, 5, 24, userId1);

        linkService.updateClickLimit(shortUrl, 10, userId1);
        Link link = linkService.getLink(shortUrl);
        assertEquals(10, link.getClickLimit(), "Лимит переходов должен быть обновлен");
    }

    @Test
    void testUpdateClickLimit_UnauthorizedUser() {
        String originalUrl = "https://example.com";
        String shortUrl = linkService.createShortLink(originalUrl, 5, 24, userId1);

        assertThrows(SecurityException.class, () -> linkService.updateClickLimit(shortUrl, 10, userId2),
                "Только создатель ссылки может редактировать её параметры");
    }

    @Test
    void testDeleteLink() {
        String originalUrl = "https://example.com";
        String shortUrl = linkService.createShortLink(originalUrl, 5, 24, userId1);

        linkService.deleteLink(shortUrl, userId1);
        assertThrows(LinkNotFoundException.class, () -> linkService.redirect(shortUrl),
                "Ссылка должна быть удалена");
    }

    @Test
    void testDeleteLink_UnauthorizedUser() {
        String originalUrl = "https://example.com";
        String shortUrl = linkService.createShortLink(originalUrl, 5, 24, userId1);

        assertThrows(SecurityException.class, () -> linkService.deleteLink(shortUrl, userId2),
                "Только создатель ссылки может удалить её");
    }

    @Test
    void testLinkExpiration() {
        String originalUrl = "https://example.com";
        String shortUrl = linkService.createShortLink(originalUrl, 5, 1, userId1); // Время жизни — 1 час

        // Устанавливаем время жизни ссылки в прошлое
        Link link = linkService.getLink(shortUrl);
        link.setExpirationTime(LocalDateTime.now().minusHours(1));

        assertThrows(LinkExpiredException.class, () -> linkService.redirect(shortUrl),
                "Ссылка должна быть недоступна после истечения времени жизни");
    }

    @Test
    void testConfigMaxValues() {
        String originalUrl = "https://example.com";
        int userClickLimit = 200; // Пользовательский лимит больше максимального
        int userLifetimeHours = 48; // Пользовательское время жизни больше максимального

        String shortUrl = linkService.createShortLink(originalUrl, userClickLimit, userLifetimeHours, userId1);
        Link link = linkService.getLink(shortUrl);

        assertEquals(Config.getMaxClickLimit(), link.getClickLimit(), "Лимит переходов должен быть ограничен максимальным значением");
        assertEquals(Config.getMaxLinkLifetimeHours(), link.getLifetimeHours(), "Время жизни ссылки должно быть ограничено максимальным значением");
    }
}