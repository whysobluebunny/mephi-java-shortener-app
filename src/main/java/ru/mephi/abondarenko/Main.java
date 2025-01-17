package ru.mephi.abondarenko;

import ru.mephi.abondarenko.config.Config;
import ru.mephi.abondarenko.model.User;
import ru.mephi.abondarenko.service.LinkService;
import ru.mephi.abondarenko.service.UserService;

import java.awt.*;
import java.net.URI;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();
        LinkService linkService = new LinkService();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nМеню:");
            System.out.println("1. Зарегистрировать нового пользователя");
            System.out.println("2. Создать короткую ссылку");
            System.out.println("3. Перейти по короткой ссылке");
            System.out.println("4. Изменить лимит переходов");
            System.out.println("5. Удалить ссылку");
            System.out.println("6. Выйти");
            System.out.print("Выберите действие: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> handleUserRegistration(userService, scanner);
                case "2" -> handleShortLinkCreation(userService, linkService, scanner);
                case "3" -> handleShortLinkRedirection(linkService, scanner);
                case "4" -> handleUpdateClickLimit(linkService, scanner);
                case "5" -> handleDeleteLink(linkService, scanner);
                case "6" -> {
                    System.out.println("Выход из приложения...");
                    scanner.close();
                    System.exit(0);
                    return;
                }
                default -> System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }
    }

    private static void handleUserRegistration(UserService userService, Scanner scanner) {
        System.out.print("Введите ваше имя: ");
        String username = scanner.nextLine();
        if (username.isBlank()) {
            System.out.println("Имя не может быть пустым.");
            return;
        }

        User user = userService.createUser(username);
        System.out.println("Пользователь зарегистрирован. Ваш ID: " + user.getUserId());
    }

    private static void handleShortLinkCreation(UserService userService, LinkService linkService, Scanner scanner) {
        System.out.print("Введите ваш ID: ");
        String userIdInput = scanner.nextLine();

        UUID userId;
        try {
            userId = UUID.fromString(userIdInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Неверный формат ID. Пожалуйста, введите корректный UUID.");
            return;
        }

        User currentUser = userService.getUser(userId);
        if (currentUser == null) {
            System.out.println("Пользователь не найден.");
            return;
        }

        System.out.print("Введите URL для сокращения: ");
        String originalUrl = scanner.nextLine();
        if (originalUrl.isBlank() || !originalUrl.startsWith("http")) {
            System.out.println("Некорректный URL. URL должен начинаться с 'http' или 'https'.");
            return;
        }

        int clickLimit = Config.getDefaultClickLimit();
        int lifetimeHours = Config.getDefaultLifetimeHours();

        String shortUrl = linkService.createShortLink(originalUrl, clickLimit, lifetimeHours, userId);
        System.out.println("Короткая ссылка: " + shortUrl);
        System.out.println("Лимит переходов: " + clickLimit);
        System.out.println("Время жизни ссылки: " + lifetimeHours + " часов");
    }

    private static void handleShortLinkRedirection(LinkService linkService, Scanner scanner) {
        System.out.print("Введите короткую ссылку: ");
        String inputShortUrl = scanner.nextLine();
        if (inputShortUrl.isBlank()) {
            System.out.println("Короткая ссылка не может быть пустой.");
            return;
        }

        try {
            String redirectedUrl = linkService.redirect(inputShortUrl);
            System.out.println("Перенаправление на: " + redirectedUrl);
            Desktop.getDesktop().browse(new URI(redirectedUrl));
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void handleUpdateClickLimit(LinkService linkService, Scanner scanner) {
        System.out.print("Введите ваш ID: ");
        String userIdInput = scanner.nextLine();

        UUID userId;
        try {
            userId = UUID.fromString(userIdInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Неверный формат ID. Пожалуйста, введите корректный UUID.");
            return;
        }

        System.out.print("Введите короткую ссылку: ");
        String shortUrl = scanner.nextLine();
        if (shortUrl.isBlank()) {
            System.out.println("Короткая ссылка не может быть пустой.");
            return;
        }

        System.out.print("Введите новый лимит переходов (текущий лимит будет заменен): ");
        int newClickLimit;
        try {
            newClickLimit = scanner.nextInt();
            if (newClickLimit <= 0) {
                System.out.println("Лимит переходов должен быть положительным числом.");
                return;
            }
        } catch (InputMismatchException e) {
            System.out.println("Неверный формат лимита переходов. Пожалуйста, введите число.");
            scanner.nextLine(); // Очистка буфера
            return;
        }
        scanner.nextLine(); // Очистка буфера

        try {
            linkService.updateClickLimit(shortUrl, newClickLimit, userId);
            System.out.println("Лимит переходов успешно обновлен.");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void handleDeleteLink(LinkService linkService, Scanner scanner) {
        System.out.print("Введите ваш ID: ");
        String userIdInput = scanner.nextLine();

        UUID userId;
        try {
            userId = UUID.fromString(userIdInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Неверный формат ID. Пожалуйста, введите корректный UUID.");
            return;
        }

        System.out.print("Введите короткую ссылку: ");
        String shortUrl = scanner.nextLine();
        if (shortUrl.isBlank()) {
            System.out.println("Короткая ссылка не может быть пустой.");
            return;
        }

        try {
            linkService.deleteLink(shortUrl, userId);
            System.out.println("Ссылка успешно удалена.");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}