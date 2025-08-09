package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * Сущность, представляющая уникальную конфигурацию тестового запуска.
 * Конфигурация является комбинацией версии приложения, окружения и тестового набора.
 * Она используется для группировки схожих тестовых запусков и их последующего анализа.
 */
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TestConfiguration {

    /**
     * Уникальный идентификатор конфигурации (генерируется автоматически).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Версия тестируемого приложения.
     */
    @Column(nullable = false)
    private String appVersion;

    /**
     * Название окружения, на котором выполнялся тест (например, "QA", "STAGING").
     */
    @Column(nullable = false)
    private String environment;

    /**
     * Название тестового набора (suite), к которому относится тест.
     */
    private String testSuite;

    /**
     * Уникальное строковое представление конфигурации, сгенерированное на основе
     * версии, окружения и набора тестов. Используется для быстрого поиска.
     */
    @Column(unique = true, length = 512)
    private String uniqueName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestConfiguration that = (TestConfiguration) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
