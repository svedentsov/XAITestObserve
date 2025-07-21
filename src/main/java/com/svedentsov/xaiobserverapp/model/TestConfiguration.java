package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сущность, представляющая конфигурацию тестового запуска.
 * Используется для хранения метаданных о среде, версии приложения и тестовом наборе,
 * на которых был выполнен тест.
 * Обеспечивает уникальность комбинации этих параметров.
 */
@Entity
@Data
@NoArgsConstructor
public class TestConfiguration {
    /**
     * Уникальный идентификатор конфигурации.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Версия приложения, на которой выполнялся тест.
     * Не может быть null.
     */
    @Column(nullable = false)
    private String appVersion;

    /**
     * Среда выполнения теста (например, "QA", "STAGING").
     * Не может быть null.
     */
    @Column(nullable = false)
    private String environment;

    /**
     * Название тестового набора, к которому относится конфигурация.
     */
    private String testSuite;

    /**
     * Уникальное имя, сгенерированное на основе {@code appVersion}, {@code environment} и {@code testSuite}.
     * Используется для обеспечения уникальности и быстрого поиска конфигураций.
     */
    @Column(unique = true)
    private String uniqueName;

    /**
     * Метод, вызываемый перед сохранением (Persist) или обновлением (Update) сущности.
     * Генерирует уникальное имя для конфигурации на основе комбинации версии приложения,
     * окружения и тестового набора.
     */
    @PrePersist
    @PreUpdate
    private void generateUniqueName() {
        this.uniqueName = String.format("%s-%s-%s", appVersion, environment, testSuite).toLowerCase();
    }
}
