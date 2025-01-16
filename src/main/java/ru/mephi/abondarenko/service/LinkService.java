package ru.mephi.abondarenko.service;

import ru.mephi.abondarenko.config.Config;
import ru.mephi.abondarenko.exception.LinkExpiredException;
import ru.mephi.abondarenko.exception.LinkNotFoundException;
import ru.mephi.abondarenko.model.Link;
import ru.mephi.abondarenko.util.LinkShortener;

import java.time.LocalDateTime;
import java.util.*;

public class LinkService {
	private final Map<String, Link> links = new HashMap<>();

	private final Timer timer = new Timer();

	public LinkService() {
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				LocalDateTime now = LocalDateTime.now();
				links.entrySet().removeIf(entry -> entry.getValue().getExpirationTime().isBefore(now));
			}
		}, 0, 60 * 1000);
	}

	public String createShortLink(String originalUrl, int clickLimit, int lifetimeHours, UUID userId) {
		int maxClickLimit = Math.min(clickLimit, Config.getMaxClickLimit());
		int maxLifetimeHours = Math.min(lifetimeHours, Config.getMaxLinkLifetimeHours());

		Link link = new Link(originalUrl, maxClickLimit, maxLifetimeHours, userId);
		String shortUrl = LinkShortener.shortenUrl(originalUrl, userId);
		links.put(shortUrl, link);
		return shortUrl;
	}

	public String redirect(String shortUrl) {
		Link link = links.get(shortUrl);
		if (link == null) {
			throw new LinkNotFoundException("Ссылка не найдена");
		}
		if (!link.isActive()) {
			throw new LinkExpiredException("Ссылка недоступна");
		}
		link.setClickCount(link.getClickCount() + 1);
		return link.getOriginalUrl();
	}

	public void updateClickLimit(String shortUrl, int newClickLimit, UUID userId) {
		Link link = links.get(shortUrl);
		if (link == null) {
			throw new LinkNotFoundException("Ссылка не найдена");
		}
		if (!link.getUserId().equals(userId)) {
			throw new SecurityException("Только создатель ссылки может редактировать её параметры");
		}
		link.setClickLimit(newClickLimit);
	}

	public void deleteLink(String shortUrl, UUID userId) {
		Link link = links.get(shortUrl);
		if (link == null) {
			throw new LinkNotFoundException("Ссылка не найдена");
		}
		if (!link.getUserId().equals(userId)) {
			throw new SecurityException("Только создатель ссылки может удалить её");
		}
		links.remove(shortUrl);
	}

	public Link getLink(String shortUrl) {
		return links.get(shortUrl);
	}
}