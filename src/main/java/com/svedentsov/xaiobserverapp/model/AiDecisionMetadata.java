package com.svedentsov.xaiobserverapp.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Метаданные о конкретном шаге выполнения теста, часто сгенерированные AI-агентом.
 * Этот класс является встраиваемым (Embeddable) и используется для хранения
 * информации о шагах в пути выполнения теста и о шаге, на котором произошел сбой.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Schema(description = "Метаданные о конкретном шаге выполнения теста, часто сгенерированные AI-агентом")
public class AiDecisionMetadata {

    @Schema(description = "Порядковый номер шага в сценарии", example = "3")
    private Integer stepNumber;

    @Schema(description = "Описание действия, выполняемого на шаге", example = "Клик по кнопке 'Войти'")
    private String action;

    @Schema(description = "Стратегия поиска элемента (css, xpath, id и т.д.)", example = "xpath")
    private String locatorStrategy;

    @Schema(description = "Значение локатора для поиска элемента", example = "//button[@type='submit']")
    @Column(length = 512)
    private String locatorValue;

    @Schema(description = "Текст, с которым взаимодействовали (например, вводимый в поле)", example = "user@example.com")
    private String interactedText;

    @Schema(description = "Уверенность AI в правильности выполнения этого шага (от 0.0 до 1.0)", example = "0.98")
    private double confidenceScore;

    @Schema(description = "Результат выполнения шага", allowableValues = {"SUCCESS", "FAILURE", "SKIPPED"}, example = "SUCCESS")
    private String result;

    @Schema(description = "Сообщение об ошибке, если шаг провалился", example = "Элемент не кликабелен")
    private String errorMessage;

    @Schema(description = "Время начала шага в мс (Unix epoch)", example = "1678886410000")
    private Long stepStartTime;

    @Schema(description = "Время окончания шага в мс (Unix epoch)", example = "1678886411500")
    private Long stepEndTime;

    @Schema(description = "Длительность шага в мс", example = "1500")
    private Long stepDurationMillis;

    @Schema(description = "Дополнительные данные шага в формате JSON", example = "{\"element_visible\": true}")
    @Column(length = 1000)
    private String additionalStepData;
}
