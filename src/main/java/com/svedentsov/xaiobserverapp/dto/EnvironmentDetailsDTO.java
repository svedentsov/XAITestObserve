package com.svedentsov.xaiobserverapp.dto;

import lombok.Data;

/**
 * DTO для хранения подробной информации о тестовом окружении.
 * Это позволяет собирать более гранулированные данные для анализа.
 */
@Data
public class EnvironmentDetailsDTO {
    /**
     * Имя среды (например, "QA", "STAGING", "PRODUCTION").
     */
    private String name;
    /**
     * Тип операционной системы (например, "Windows 10", "macOS Ventura", "Linux Ubuntu").
     */
    private String osType;
    /**
     * Версия операционной системы.
     */
    private String osVersion;
    /**
     * Тип браузера (например, "Chrome", "Firefox", "Edge").
     */
    private String browserType;
    /**
     * Версия браузера.
     */
    private String browserVersion;
    /**
     * Разрешение экрана, на котором выполнялся тест (например, "1920x1080").
     */
    private String screenResolution;
    /**
     * Тип устройства (например, "Desktop", "Mobile", "Tablet").
     */
    private String deviceType;
    /**
     * Название устройства или эмулятора (для мобильных тестов).
     */
    private String deviceName;
    /**
     * Версия драйвера (например, ChromeDriver, GeckoDriver).
     */
    private String driverVersion;
    /**
     * URL базового приложения, на котором выполнялся тест.
     */
    private String appBaseUrl;
}
