package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.DailyTrendDataDTO;
import com.svedentsov.xaiobserverapp.dto.DashboardStatisticsDTO;
import com.svedentsov.xaiobserverapp.dto.SlowTestDTO;
import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.repository.TestRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Сервис для расчета и предоставления расширенных статистических данных по тестовым запускам.
 * Результаты вычислений кэшируются для повышения производительности.
 * Все тяжелые вычисления делегируются базе данных через JPQL-запросы для максимальной эффективности.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private static final int TOP_LIMIT = 10;
    private static final int TREND_DAYS = 30;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TestRunRepository testRunRepository;

    /**
     * Рассчитывает и возвращает полную статистику для дашборда.
     * <p>
     * Результат этого метода кэшируется в кэше "dashboard_statistics". Кэш
     * сбрасывается при поступлении нового тестового запуска или удалении данных.
     *
     * @return {@link DashboardStatisticsDTO} со всей необходимой статистикой.
     */
    @Transactional(readOnly = true)
    @Cacheable("dashboard_statistics")
    public DashboardStatisticsDTO getDashboardStatistics() {
        log.info("Calculating new dashboard statistics (cache miss).");

        long totalRuns = testRunRepository.count();
        if (totalRuns == 0) {
            return createEmptyStatistics();
        }

        long passedRuns = testRunRepository.countByStatus(TestRun.TestStatus.PASSED);
        long failedRuns = testRunRepository.countByStatus(TestRun.TestStatus.FAILED);
        long skippedRuns = testRunRepository.countByStatus(TestRun.TestStatus.SKIPPED) +
                testRunRepository.countByStatus(TestRun.TestStatus.BROKEN);
        double passRate = (totalRuns > 0) ? (double) passedRuns / totalRuns * 100.0 : 0.0;

        // Новые метрики
        double averageDuration = testRunRepository.findAverageTestDuration().orElse(0.0);
        long uniqueTests = testRunRepository.countDistinctTests();

        Map<String, Long> topFailingTests = convertDbResultToMap(testRunRepository.findTopFailingTests(PageRequest.of(0, TOP_LIMIT)), "testName", "failureCount");

        String mostUnstableTest = topFailingTests.keySet().stream().findFirst().orElse(null);
        if (StringUtils.hasText(mostUnstableTest)) {
            // Укорачиваем имя для лучшего отображения
            mostUnstableTest = mostUnstableTest.substring(mostUnstableTest.lastIndexOf('.') + 1);
        }

        Map<String, Long> runsBySuite = convertDbResultToMap(testRunRepository.countRunsBySuite(), "name", "count");
        Map<String, Long> runsByEnvironment = convertDbResultToMap(testRunRepository.countRunsByEnvironment(), "name", "count");
        Map<String, Long> topExceptionTypes = convertDbResultToMap(testRunRepository.findTopExceptionTypes(PageRequest.of(0, 5)), "name", "count");

        List<SlowTestDTO> topSlowTests = testRunRepository.findTopSlowestTests(PageRequest.of(0, 5)).stream()
                .map(map -> new SlowTestDTO((String) map.get("testName"), ((Number) map.get("avgDuration")).doubleValue())).toList();

        List<DailyTrendDataDTO> dailyPassRateTrend = calculateDailyPassRateTrend();

        return new DashboardStatisticsDTO(totalRuns, passedRuns, failedRuns, skippedRuns, passRate,
                averageDuration, uniqueTests, mostUnstableTest,
                dailyPassRateTrend, topFailingTests, topSlowTests, runsBySuite, runsByEnvironment, topExceptionTypes);
    }

    /**
     * Принудительно очищает кэш статистики.
     * Вызывается после операций, изменяющих данные (сохранение, удаление).
     */
    @CacheEvict(value = "dashboard_statistics", allEntries = true)
    public void clearStatisticsCache() {
        log.info("Dashboard statistics cache has been cleared.");
    }

    /**
     * Создает пустой объект статистики, когда в базе данных нет записей.
     *
     * @return Пустой DTO статистики.
     */
    private DashboardStatisticsDTO createEmptyStatistics() {
        return new DashboardStatisticsDTO(0, 0, 0, 0, 0.0, 0.0, 0, null, Collections.emptyList(), Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    /**
     * Вспомогательный метод для преобразования результата JPQL-запроса (списка карт)
     * в упорядоченную карту (LinkedHashMap).
     *
     * @param dbResult  Результат из репозитория.
     * @param keyName   Имя ключа в карте результата.
     * @param valueName Имя значения в карте результата.
     * @return {@link Map} с отфильтрованными и преобразованными данными.
     */
    private Map<String, Long> convertDbResultToMap(List<Map<String, Object>> dbResult, String keyName, String valueName) {
        return dbResult.stream()
                .filter(map -> map.get(keyName) != null)
                .collect(Collectors.toMap(
                        map -> (String) map.get(keyName),
                        map -> ((Number) map.get(valueName)).longValue(),
                        (v1, v2) -> v1,
                        java.util.LinkedHashMap::new
                ));
    }

    /**
     * Рассчитывает данные для тренда Pass Rate за последние N дней.
     *
     * @return Список DTO для построения графика.
     */
    private List<DailyTrendDataDTO> calculateDailyPassRateTrend() {
        final var sinceDate = LocalDateTime.now().minusDays(TREND_DAYS);
        var trendDataFromDb = testRunRepository.findDailyTrendData(sinceDate);

        Map<LocalDate, DailyTrendDataDTO> trendMap = trendDataFromDb.stream()
                .collect(Collectors.toMap(
                        map -> convertToLocalDate(map.get("runDate")),
                        map -> {
                            LocalDate runDate = convertToLocalDate(map.get("runDate"));
                            long total = ((Number) map.get("totalCount")).longValue();
                            long passed = ((Number) map.get("passedCount")).longValue();
                            double rate = (total > 0) ? (double) passed / total * 100.0 : 0.0;
                            String formattedDate = runDate.format(DATE_FORMATTER);
                            return new DailyTrendDataDTO(formattedDate, rate, total);
                        }
                ));

        final var startDate = sinceDate.toLocalDate();
        return LongStream.range(0, TREND_DAYS)
                .mapToObj(startDate::plusDays)
                .map(date -> trendMap.getOrDefault(date, new DailyTrendDataDTO(date.format(DATE_FORMATTER), 0.0, 0)))
                .toList();
    }

    /**
     * Безопасно конвертирует объект, полученный из JDBC, в {@link LocalDate}.
     *
     * @param dateObject объект даты из результата запроса.
     * @return сконвертированный {@link LocalDate}.
     */
    private LocalDate convertToLocalDate(Object dateObject) {
        return switch (dateObject) {
            case null -> throw new IllegalStateException("runDate не может быть null в данных для тренда");
            case Date date -> date.toLocalDate();
            case LocalDate localDate -> localDate;
            default -> {
                log.warn("Неожиданный тип даты {} в данных для тренда, попытка парсинга из строки", dateObject.getClass());
                try {
                    yield LocalDate.parse(dateObject.toString());
                } catch (java.time.format.DateTimeParseException e) {
                    log.error("Не удалось разобрать объект даты: {}", dateObject, e);
                    throw new IllegalStateException("Неподдерживаемый тип или формат даты: " + dateObject.getClass().getName());
                }
            }
        };
    }
}
