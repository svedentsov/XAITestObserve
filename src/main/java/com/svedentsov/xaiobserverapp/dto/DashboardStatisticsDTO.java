package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * DTO (Data Transfer Object) для передачи расширенной агрегированной статистики
 * на дашборд. Этот объект содержит все метрики, необходимые для визуализации
 * на вкладке "Статистика".
 *
 * @param totalRuns           Общее количество тестовых запусков в системе.
 * @param passedRuns          Количество запусков со статусом "PASSED".
 * @param failedRuns          Количество запусков со статусом "FAILED".
 * @param skippedRuns         Количество запусков со статусом "SKIPPED" или "BROKEN".
 * @param passRate            Процент успешных запусков (отношение passedRuns к totalRuns).
 * @param averageTestDuration Средняя продолжительность выполнения всех тестов в миллисекундах.
 * @param uniqueTestCount     Общее количество уникальных тестов (по комбинации класс + метод).
 * @param mostUnstableTest    Название теста, который падал чаще всего.
 * @param dailyPassRateTrend  Данные для построения графика тренда Pass Rate по дням.
 * @param topFailingTests     Карта, содержащая самые нестабильные тесты и количество их падений.
 * @param topSlowTests        Список самых медленных тестов с их средней продолжительностью.
 * @param runsBySuite         Карта с распределением количества запусков по тестовым наборам (suites).
 * @param runsByEnvironment   Карта с распределением количества запусков по окружениям.
 * @param topExceptionTypes   Карта, содержащая самые частые типы исключений и их количество.
 */
@Schema(description = "Расширенная статистика для дашборда")
public record DashboardStatisticsDTO(

        @Schema(description = "Общее количество запусков")
        long totalRuns,

        @Schema(description = "Количество успешных запусков")
        long passedRuns,

        @Schema(description = "Количество проваленных запусков")
        long failedRuns,

        @Schema(description = "Количество пропущенных запусков")
        long skippedRuns,

        @Schema(description = "Процент успешных запусков (Pass Rate)")
        double passRate,

        @Schema(description = "Средняя длительность всех тестов в мс")
        double averageTestDuration,

        @Schema(description = "Общее количество уникальных тестов")
        long uniqueTestCount,

        @Schema(description = "Самый нестабильный тест")
        String mostUnstableTest,

        @Schema(description = "Тренд Pass Rate по дням")
        List<DailyTrendDataDTO> dailyPassRateTrend,

        @Schema(description = "Топ-10 самых нестабильных тестов")
        Map<String, Long> topFailingTests,

        @Schema(description = "Топ-5 самых медленных тестов")
        List<SlowTestDTO> topSlowTests,

        @Schema(description = "Распределение запусков по тестовым наборам")
        Map<String, Long> runsBySuite,

        @Schema(description = "Распределение запусков по окружениям")
        Map<String, Long> runsByEnvironment,

        @Schema(description = "Топ-5 типов исключений в проваленных тестах")
        Map<String, Long> topExceptionTypes
) {
}
