package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO для отправки обратной связи от пользователя по качеству AI-анализа.
 * Используется для сбора данных для улучшения моделей анализа.
 */
@Data
@Schema(description = "DTO для отправки обратной связи от пользователя по качеству AI-анализа")
public class AnalysisFeedbackDTO {

    @Schema(description = "Был ли предложенный AI анализ верным?", required = true, example = "true")
    private Boolean isAiSuggestionCorrect;

    @Schema(description = "Причина сбоя, предложенная пользователем (если AI ошибся)", example = "Проблема была не в локаторе, а в A/B тесте, который менял цвет кнопки.")
    private String userProvidedReason;

    @Schema(description = "Решение, предложенное пользователем", example = "Нужно добавить проверку на наличие A/B теста и использовать разные локаторы.")
    private String userProvidedSolution;

    @Schema(description = "Дополнительные комментарии", example = "Отличная попытка, AI!")
    private String comments;

    @Schema(description = "Идентификатор пользователя, оставившего отзыв (опционально)", example = "test_engineer_1")
    private String userId;
}
