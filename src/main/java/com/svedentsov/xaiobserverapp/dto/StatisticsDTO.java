package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * DTO для агрегированной статистики по всем тестовым запускам.
 * Содержит общие метрики, списки проблемных тестов и данные для трендов.
 *
 * @param totalRuns          Общее количество запусков.
 * @param passedRuns         Количество успешных запусков.
 * @param failedRuns         Количество проваленных запусков.
 * @param skippedRuns        Количество пропущенных или сломанных запусков.
 * @param passRate           Процент успешных запусков (Pass Rate).
 * @param failureCountByTest Топ самых нестабильных тестов (тест: количество падений).
 * @param dailyPassRateTrend Тренд Pass Rate по дням.
 * @param topSlowTests       Топ самых медленных тестов.
 */
@Schema(description = "Общая статистика по всем тестовым запускам")
public record StatisticsDTO(

        @Schema(description = "Общее количество запусков", example = "1520")
        long totalRuns,

        @Schema(description = "Количество успешных запусков", example = "1200")
        long passedRuns,

        @Schema(description = "Количество проваленных запусков", example = "250")
        long failedRuns,

        @Schema(description = "Количество пропущенных или сломанных запусков", example = "70")
        long skippedRuns,

        @Schema(description = "Процент успешных запусков (Pass Rate)", example = "78.9")
        double passRate,

        @Schema(description = "Топ-10 самых нестабильных тестов (тест: количество падений)", example = "{\"com.tests.Checkout.testGuestCheckout\": 55}")
        Map<String, Long> failureCountByTest,

        @Schema(description = "Тренд Pass Rate по дням за последний месяц")
        List<DailyTrendDataDTO> dailyPassRateTrend,

        @Schema(description = "Топ-5 самых медленных тестов")
        List<SlowTestDTO> topSlowTests
) {
}
