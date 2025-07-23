package com.svedentsov.xaiobserverapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Объект передачи данных (DTO) для общей статистики по тестовым запускам.
 * Включает агрегированные данные о количестве запусков, проценте прохождения,
 * и данные для построения графиков трендов и самых медленных тестов.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsDTO {
    /**
     * Общее количество выполненных тестовых запусков.
     */
    private long totalRuns;
    /**
     * Количество успешно пройденных тестовых запусков.
     */
    private long passedRuns;
    /**
     * Количество проваленных тестовых запусков.
     */
    private long failedRuns;
    /**
     * Количество пропущенных тестовых запусков.
     */
    private long skippedRuns;
    /**
     * Процент прохождения тестов ({@code passedRuns / totalRuns * 100}).
     */
    private double passRate;
    /**
     * Карта, содержащая количество сбоев по каждому тестовому методу.
     * Ключ: имя тестового метода, Значение: количество сбоев.
     */
    private Map<String, Long> failureCountByTest;

    /**
     * Список данных для построения графика ежедневного тренда процента прохождения.
     */
    private List<DailyTrendData> dailyPassRateTrend;
    /**
     * Список данных для отображения самых медленных тестов.
     */
    private List<SlowTestDTO> topSlowTests;

    /**
     * Внутренний DTO для данных о ежедневном тренде.
     * Содержит дату и соответствующий процент прохождения за этот день.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyTrendData {
        /**
         * Дата в формате "YYYY-MM-DD".
         */
        private String date;
        /**
         * Процент прохождения за указанную дату.
         */
        private double passRate;
        /**
         * Общее количество запусков за указанную дату.
         */
        private long totalRuns;
    }

    /**
     * Внутренний DTO для данных о медленных тестах.
     * Содержит имя теста и его среднюю длительность.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SlowTestDTO {
        /**
         * Имя медленного тестового метода.
         */
        private String testName;
        /**
         * Средняя продолжительность выполнения теста в миллисекундах.
         * (Предполагается, что в будущем тесты будут иметь длительность).
         */
        private double averageDurationMillis;
    }
}
