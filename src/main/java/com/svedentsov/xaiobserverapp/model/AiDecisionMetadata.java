package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Встраиваемая сущность (Embeddable) для хранения метаданных о действиях AI
 * или о конкретном шаге выполнения теста.
 * Используется для детализации действий, локаторов, результатов,
 * а также для оценки уверенности AI в своих предсказаниях и сбора дополнительной информации.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AiDecisionMetadata {
    /**
     * Порядковый номер шага в тестовом сценарии.
     */
    private Integer stepNumber;
    /**
     * Описание действия, выполненного на данном шаге (например, "Навигация", "Клик", "Ввод текста").
     */
    private String action;
    /**
     * Стратегия поиска элемента (например, "id", "xpath", "css selector", "name").
     */
    private String locatorStrategy;
    /**
     * Значение локатора, используемого для поиска элемента.
     * Может быть довольно длинным, поэтому увеличена длина столбца.
     */
    @Column(length = 512)
    private String locatorValue;
    /**
     * Текст, с которым взаимодействовал пользователь или тест (например, введенный текст, текст кнопки).
     */
    private String interactedText;
    /**
     * Показатель уверенности AI в правильности этого шага или предсказания (от 0.0 до 1.0).
     */
    private double confidenceScore;
    /**
     * Результат выполнения шага (например, "SUCCESS", "FAILURE", "SKIPPED", "PENDING").
     */
    private String result;
    /**
     * Сообщение об ошибке, если шаг завершился сбоем.
     */
    private String errorMessage;
    /**
     * Время начала выполнения шага в миллисекундах.
     */
    private Long stepStartTime;
    /**
     * Время завершения выполнения шага в миллисекундах.
     */
    private Long stepEndTime;
    /**
     * Длительность выполнения шага в миллисекундах.
     */
    private Long stepDurationMillis;
    /**
     * Дополнительные произвольные метаданные для конкретного шага.
     */
    @Column(length = 1000)
    private String additionalStepData;
}
