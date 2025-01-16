package ru.mephi.abondarenko.service;

import ru.mephi.abondarenko.exception.LinkExpiredException;
import ru.mephi.abondarenko.exception.LinkNotFoundException;
import ru.mephi.abondarenko.model.Link;
import ru.mephi.abondarenko.util.LinkShortener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LinkService {
    private final Map<String, Link> links = new HashMap<>();

    public String createShortLink(String originalUrl, int clickLimit, UUID userId) {
        Link link = new Link(originalUrl, clickLimit, userId);
        String shortUrl = LinkShortener.shortenUrl(originalUrl, userId); // Передаем userId
        links.put(shortUrl, link);
        return shortUrl;
    }

    public String redirect(String shortUrl) {
        Link link = links.get(shortUrl);
        if (link == null) {
            throw new LinkNotFoundException("Ссылка не найдена");
        }
        if (link.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new LinkExpiredException("Срок действия ссылки истек");
        }
        if (link.getClickCount() >= link.getClickLimit()) {
            throw new LinkExpiredException("Лимит переходов исчерпан");
        }
        link.setClickCount(link.getClickCount() + 1);
        return link.getOriginalUrl();
    }

    public Link getLink(String shortUrl) {
        return links.get(shortUrl);
    }
}