package ru.mephi.abondarenko.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки конфигурационного файла: " + e.getMessage());
        }
    }

    public static int getMaxLinkLifetimeHours() {
        return Integer.parseInt(properties.getProperty("max_link_lifetime_hours", "24"));
    }

    public static int getMaxClickLimit() {
        return Integer.parseInt(properties.getProperty("max_click_limit", "100"));
    }

    public static int getDefaultClickLimit() {
        return Integer.parseInt(properties.getProperty("default_click_limit", "10"));
    }

    public static int getDefaultLifetimeHours() {
        return Integer.parseInt(properties.getProperty("default_lifetime_hours", "24"));
    }
}