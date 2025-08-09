package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Встраиваемый класс (Embeddable) для хранения детальной информации об окружении,
 * в котором выполнялся тест. Эти данные встраиваются непосредственно в таблицу {@link TestRun}.
 */
@Embeddable
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddableEnvironmentDetails {
    /**
     * Имя окружения (например, QA, STAGING).
     */
    @Column(name = "env_name")
    private String name;
    /**
     * Тип операционной системы.
     */
    private String osType;
    /**
     * Версия операционной системы.
     */
    private String osVersion;
    /**
     * Тип браузера.
     */
    private String browserType;
    /**
     * Версия браузера.
     */
    private String browserVersion;
    /**
     * Разрешение экрана.
     */
    private String screenResolution;
    /**
     * Тип устройства (Desktop, Mobile).
     */
    private String deviceType;
    /**
     * Название устройства (актуально для мобильных).
     */
    private String deviceName;
    /**
     * Версия WebDriver.
     */
    private String driverVersion;
    /**
     * Базовый URL тестируемого приложения.
     */
    private String appBaseUrl;
}
