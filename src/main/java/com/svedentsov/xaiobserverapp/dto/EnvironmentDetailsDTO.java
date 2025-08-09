package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для передачи детальной информации об окружении, в котором выполнялся тест.
 *
 * @param name             Название окружения (например, QA, STAGING).
 * @param osType           Тип операционной системы.
 * @param osVersion        Версия операционной системы.
 * @param browserType      Тип браузера.
 * @param browserVersion   Версия браузера.
 * @param screenResolution Разрешение экрана.
 * @param deviceType       Тип устройства (Desktop, Mobile).
 * @param deviceName       Название устройства (для мобильных).
 * @param driverVersion    Версия WebDriver.
 * @param appBaseUrl       Базовый URL тестируемого приложения.
 */
@Schema(description = "Детали окружения, в котором выполнялся тест")
public record EnvironmentDetailsDTO(

        @Schema(description = "Название окружения", example = "QA-STAND")
        String name,

        @Schema(description = "Тип операционной системы", example = "Windows")
        String osType,

        @Schema(description = "Версия операционной системы", example = "11")
        String osVersion,

        @Schema(description = "Тип браузера", example = "Chrome")
        String browserType,

        @Schema(description = "Версия браузера", example = "126.0.6478.127")
        String browserVersion,

        @Schema(description = "Разрешение экрана", example = "1920x1080")
        String screenResolution,

        @Schema(description = "Тип устройства", example = "Desktop")
        String deviceType,

        @Schema(description = "Название устройства (для мобильных)", example = "iPhone 15 Pro")
        String deviceName,

        @Schema(description = "Версия WebDriver", example = "126.0.6478.127")
        String driverVersion,

        @Schema(description = "Базовый URL тестируемого приложения", example = "https://qa.myapp.com")
        String appBaseUrl
) {
}
