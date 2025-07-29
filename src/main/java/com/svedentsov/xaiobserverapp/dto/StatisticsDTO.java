package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO для передачи общей статистики по всем тестовым запускам.
 * Включает в себя счетчики, тренды и топы нестабильных/медленных тестов.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Общая статистика по всем тестовым запускам")
public class StatisticsDTO {

    @Schema(description = "Общее количество запусков", example = "1520")
    private long totalRuns;

    @Schema(description = "Количество успешных запусков", example = "1200")
    private long passedRuns;

    @Schema(description = "Количество проваленных запусков", example = "250")
    private long failedRuns;

    @Schema(description = "Количество пропущенных или сломанных запусков", example = "70")
    private long skippedRuns;

    @Schema(description = "Процент успешных запусков (Pass Rate)", example = "78.9")
    private double passRate;

    @Schema(description = "Топ-10 самых нестабильных тестов (тест: количество падений)", example = "{\"com.tests.Checkout.testGuestCheckout\": 55}")
    private Map<String, Long> failureCountByTest;

    @Schema(description = "Тренд Pass Rate по дням за последний месяц")
    private List<DailyTrendData> dailyPassRateTrend;

    @Schema(description = "Топ-5 самых медленных тестов")
    private List<SlowTestDTO> topSlowTests;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Данные для дневного тренда")
    public static class DailyTrendData {
        @Schema(description = "Дата в формате YYYY-MM-DD", example = "2024-07-29")
        private String date;
        @Schema(description = "Pass Rate за этот день", example = "85.5")
        private double passRate;
        @Schema(description = "Всего запусков за этот день", example = "50")
        private long totalRuns;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Информация о медленном тесте")
    public static class SlowTestDTO {
        @Schema(description = "Полное имя теста", example = "com.tests.Profile.testFullProfileUpdate")
        private String testName;
        @Schema(description = "Средняя длительность выполнения в миллисекундах", example = "45789.5")
        private double averageDurationMillis;
    }
}
