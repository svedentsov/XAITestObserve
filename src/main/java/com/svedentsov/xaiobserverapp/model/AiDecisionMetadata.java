package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Встраиваемая сущность (Embeddable) для хранения метаданных о решении AI
 * или о конкретном шаге выполнения теста.
 * Используется для детализации действий, локаторов и результатов,
 * а также для оценки уверенности AI в своих предсказаниях.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AiDecisionMetadata {
    /**
     * Описание действия, выполненного на данном шаге (например, "Навигация", "Клик").
     */
    private String action;
    /**
     * Стратегия поиска элемента (например, "id", "xpath", "css").
     */
    private String locatorStrategy;
    /**
     * Значение локатора, используемого для поиска элемента.
     * Может быть довольно длинным, поэтому увеличена длина столбца.
     */
    @Column(length = 512)
    private String locatorValue;
    /**
     * Показатель уверенности AI в правильности этого шага или предсказания (от 0.0 до 1.0).
     */
    private double confidenceScore;
    /**
     * Результат выполнения шага (например, "SUCCESS", "FAILURE").
     */
    private String result;
}
