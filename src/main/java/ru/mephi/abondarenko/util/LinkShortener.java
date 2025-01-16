package ru.mephi.abondarenko.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LinkShortener {
    private static final String BASE_URL = "clck.ru/";
    private static final Map<String, String> urlMap = new HashMap<>();

    public static String shortenUrl(String originalUrl, UUID userId) {
        String uniqueKey = originalUrl + userId.toString();
        String shortUrl = BASE_URL + generateHash(uniqueKey);
        urlMap.put(shortUrl, originalUrl);
        return shortUrl;
    }

    private static String generateHash(String input) {
        return Integer.toHexString(input.hashCode()).substring(0, 6);
    }

    public static String getOriginalUrl(String shortUrl) {
        return urlMap.get(shortUrl);
    }
}