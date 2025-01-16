package ru.mephi.abondarenko;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    void testCreateShortLinkUniqueForDifferentUsers() {
        String originalUrl = "https://example.com";
        String shortUrl1 = linkService.createShortLink(originalUrl, 5, userId1);
        String shortUrl2 = linkService.createShortLink(originalUrl, 5, userId2);

        assertNotEquals(shortUrl1, shortUrl2, "Ссылки должны быть уникальными для разных пользователей");
    }

    @Test
    void testRedirectWithinClickLimit() {
        String originalUrl = "https://example.com";
        String shortUrl = linkService.createShortLink(originalUrl, 2, userId1);

        assertEquals(originalUrl, linkService.redirect(shortUrl), "Первый переход должен быть успешным");
        assertEquals(originalUrl, linkService.redirect(shortUrl), "Второй переход должен быть успешным");

        assertThrows(LinkExpiredException.class, () -> linkService.redirect(shortUrl),
            "Третий переход должен выбросить исключение (лимит исчерпан)");
    }

    @Test
    void testRedirectLinkExpired() {
        String originalUrl = "https://example.com";
        String shortUrl = linkService.createShortLink(originalUrl, 5, userId1);

        // Устанавливаем время жизни ссылки в прошлое
        Link link = linkService.getLink(shortUrl);
        link.setExpirationTime(LocalDateTime.now().minusDays(1));

        assertThrows(LinkExpiredException.class, () -> linkService.redirect(shortUrl),
            "Переход по просроченной ссылке должен выбросить исключение");
    }

    @Test
    void testRedirectLinkNotFound() {
        String nonExistentShortUrl = "clck.ru/123456";

        assertThrows(LinkNotFoundException.class, () -> linkService.redirect(nonExistentShortUrl),
            "Переход по несуществующей ссылке должен выбросить исключение");
    }

    @Test
    void testRedirectAfterExpiration() {
        String originalUrl = "https://example.com";
        String shortUrl = linkService.createShortLink(originalUrl, 5, userId1);

        // Устанавливаем время жизни ссылки в прошлое
        Link link = linkService.getLink(shortUrl);
        link.setExpirationTime(LocalDateTime.now().minusDays(1));

        assertThrows(LinkExpiredException.class, () -> linkService.redirect(shortUrl),
            "Переход по ссылке с истекшим сроком должен выбросить исключение");
    }

    @Test
    void testMultipleUsers() {
        String originalUrl = "https://example.com";
        String shortUrl1 = linkService.createShortLink(originalUrl, 5, userId1);
        String shortUrl2 = linkService.createShortLink(originalUrl, 5, userId2);

        assertNotEquals(shortUrl1, shortUrl2, "Ссылки должны быть уникальными для разных пользователей");

        assertEquals(originalUrl, linkService.redirect(shortUrl1), "Первый пользователь должен иметь доступ к своей ссылке");
        assertEquals(originalUrl, linkService.redirect(shortUrl2), "Второй пользователь должен иметь доступ к своей ссылке");
    }
}