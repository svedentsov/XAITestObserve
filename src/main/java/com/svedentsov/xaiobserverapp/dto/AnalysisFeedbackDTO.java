package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO (Data Transfer Object) для передачи обратной связи от пользователя
 * по качеству проведенного AI-анализа.
 *
 * @param isAiSuggestionCorrect Был ли предложенный AI анализ верным?
 * @param userProvidedReason    Причина сбоя, предложенная пользователем (если AI ошибся).
 * @param userProvidedSolution  Решение, предложенное пользователем.
 * @param comments              Дополнительные комментарии.
 * @param userId                Идентификатор пользователя, оставившего отзыв (опционально).
 */
@Schema(description = "DTO для отправки обратной связи от пользователя по качеству AI-анализа")
public record AnalysisFeedbackDTO(

        @NotNull(message = "isAiSuggestionCorrect must not be null")
        @Schema(description = "Был ли предложенный AI анализ верным?", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        Boolean isAiSuggestionCorrect,

        @Schema(description = "Причина сбоя, предложенная пользователем (если AI ошибся)", example = "Проблема была не в локаторе, а в A/B тесте, который менял цвет кнопки.")
        String userProvidedReason,

        @Schema(description = "Решение, предложенное пользователем", example = "Нужно добавить проверку на наличие A/B теста и использовать разные локаторы.")
        String userProvidedSolution,

        @Schema(description = "Дополнительные комментарии", example = "Отличная попытка, AI!")
        String comments,

        @Schema(description = "Идентификатор пользователя, оставившего отзыв (опционально)", example = "test_engineer_1")
        String userId
) {
}
