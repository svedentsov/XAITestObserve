package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для представления данных о тренде за один день.
 * Используется для построения графиков, например, тренда Pass Rate.
 *
 * @param date      Дата в формате YYYY-MM-DD.
 * @param passRate  Pass Rate за этот день.
 * @param totalRuns Всего запусков за этот день.
 */
@Schema(description = "Данные для дневного тренда")
public record DailyTrendDataDTO(

        @Schema(description = "Дата в формате YYYY-MM-DD", example = "2024-07-29")
        String date,

        @Schema(description = "Pass Rate за этот день", example = "85.5")
        double passRate,

        @Schema(description = "Всего запусков за этот день", example = "50")
        long totalRuns
) {
}
