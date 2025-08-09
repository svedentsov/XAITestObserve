package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * DTO для представления результата AI-анализа в ответах API.
 * Содержит информацию, которую сгенерировала система анализа причин сбоев (RCA).
 *
 * @param id              Уникальный ID результата анализа (UUID).
 * @param analysisType    Тип проведенного анализа (например, по типу исключения).
 * @param suggestedReason Предполагаемая причина сбоя, определенная AI.
 * @param solution        Предлагаемое решение проблемы.
 * @param aiConfidence    Уверенность AI в данном анализе (от 0.0 до 1.0).
 * @param explanationData Структурированные данные объяснения (например, веса признаков от LIME/SHAP).
 */
@Schema(description = "Результат AI-анализа для конкретного тестового запуска")
public record AnalysisResultDTO(

        @Schema(description = "Уникальный ID результата анализа (UUID)", example = "f0a1b2c3-d4e5-f678-90a1-b2c3d4e5f678")
        String id,

        @Schema(description = "Тип проведенного анализа", example = "Анализ по типу исключения")
        String analysisType,

        @Schema(description = "Предполагаемая причина сбоя, определенная AI", example = "Элемент не был найден, т.к. локатор устарел после обновления UI.")
        String suggestedReason,

        @Schema(description = "Предлагаемое решение проблемы", example = "1. Обновите локатор элемента. 2. Добавьте явное ожидание (WebDriverWait).")
        String solution,

        @Schema(description = "Уверенность AI в данном анализе (от 0.0 до 1.0)", example = "0.95")
        Double aiConfidence,

        @Schema(description = "Структурированные данные объяснения (например, веса признаков от LIME/SHAP)",
                example = "{\"type\": \"LIME\", \"feature_importances\": {\"exceptionType_TimeoutException\": 0.6, \"durationMillis_>_30000\": 0.2}}")
        Map<String, Object> explanationData
) {
}
