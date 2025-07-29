package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO, содержащий детальную информацию об окружении, в котором выполнялся тест.
 * Этот класс является встраиваемым (Embeddable).
 */
@Data
@Schema(description = "Детали окружения, в котором выполнялся тест")
public class EnvironmentDetailsDTO {

    @Schema(description = "Название окружения", example = "QA-STAND")
    private String name;

    @Schema(description = "Тип операционной системы", example = "Windows")
    private String osType;

    @Schema(description = "Версия операционной системы", example = "11")
    private String osVersion;

    @Schema(description = "Тип браузера", example = "Chrome")
    private String browserType;

    @Schema(description = "Версия браузера", example = "126.0.6478.127")
    private String browserVersion;

    @Schema(description = "Разрешение экрана", example = "1920x1080")
    private String screenResolution;

    @Schema(description = "Тип устройства", example = "Desktop")
    private String deviceType;

    @Schema(description = "Название устройства (для мобильных)", example = "iPhone 15 Pro")
    private String deviceName;

    @Schema(description = "Версия WebDriver", example = "126.0.6478.127")
    private String driverVersion;

    @Schema(description = "Базовый URL тестируемого приложения", example = "https://qa.myapp.com")
    private String appBaseUrl;
}
