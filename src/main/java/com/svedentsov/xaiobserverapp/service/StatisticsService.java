package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.StatisticsDTO;
import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.repository.TestRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис для сбора и предоставления различных статистических данных о тестовых запусках.
 * Включает общую статистику, тренды процента прохождения и список самых медленных тестов.
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final TestRunRepository testRunRepository;

    /**
     * Получает общую статистику по всем тестовым запускам.
     * Рассчитывает общее количество, пройденные, проваленные, пропущенные тесты,
     * процент прохождения, а также топ падающих тестов, ежедневный тренд процента прохождения
     * и самые медленные тесты.
     *
     * @return Объект {@link StatisticsDTO}, содержащий всю агрегированную статистику.
     */
    @Transactional(readOnly = true)
    public StatisticsDTO getOverallStatistics() {
        List<TestRun> allRuns = testRunRepository.findAll();
        long total = allRuns.size();
        if (total == 0) {
            // Возвращаем пустой DTO, если нет данных
            return new StatisticsDTO(0, 0, 0, 0, 0.0, Map.of(), List.of(), List.of());
        }

        long passed = allRuns.stream().filter(r -> r.getStatus() == TestRun.TestStatus.PASSED).count();
        long failed = allRuns.stream().filter(r -> r.getStatus() == TestRun.TestStatus.FAILED).count();
        // Рассчитываем пропущенные на основе общего, пройденных и проваленных
        long skipped = total - passed - failed;
        double passRate = (total > 0) ? (double) passed / total * 100.0 : 0.0;

        // Получаем топ падающих тестов (существующая логика)
        Map<String, Long> failingTests = testRunRepository.findTopFailingTests().stream()
                .limit(10) // Ограничиваем до топ-10
                .collect(Collectors.toMap(
                        record -> (String) record[0],
                        record -> (Long) record[1],
                        (oldValue, newValue) -> oldValue, // Функция слияния для дубликатов (не должно происходить при GROUP BY)
                        LinkedHashMap::new // Сохраняем порядок
                ));

        // --- Новое: Ежедневный тренд процента прохождения (например, последние 30 дней) ---
        List<StatisticsDTO.DailyTrendData> dailyPassRateTrend = calculateDailyPassRateTrend(allRuns, 30);

        // --- Новое: Топ медленных тестов (пока что фиктивные данные, требует поля длительности в TestRun) ---
        List<StatisticsDTO.SlowTestDTO> topSlowTests = calculateTopSlowTests(allRuns, 5); // Топ 5

        return new StatisticsDTO(total, passed, failed, skipped, passRate, failingTests, dailyPassRateTrend, topSlowTests);
    }

    /**
     * Вычисляет ежедневный тренд процента прохождения тестов за указанное количество дней.
     * Группирует тестовые запуски по дате и рассчитывает процент прохождения для каждого дня.
     * Заполняет данные для дней без запусков нулевыми значениями.
     *
     * @param allRuns Список всех тестовых запусков для анализа.
     * @param days    Количество дней, за которые нужно рассчитать тренд (включая текущий день).
     * @return Список объектов {@link StatisticsDTO.DailyTrendData}, представляющих ежедневный тренд.
     */
    private List<StatisticsDTO.DailyTrendData> calculateDailyPassRateTrend(List<TestRun> allRuns, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1); // Возвращаемся на 'days' дней, включая сегодня

        // Группируем запуски по дате, фильтруя только те, что попадают в заданный диапазон
        Map<LocalDate, List<TestRun>> runsByDate = allRuns.stream()
                .filter(run -> run.getTimestamp() != null &&
                        run.getTimestamp().toLocalDate().isAfter(startDate.minusDays(1)) && // Включаем startDate
                        run.getTimestamp().toLocalDate().isBefore(endDate.plusDays(1))) // Включаем endDate
                .collect(Collectors.groupingBy(run -> run.getTimestamp().toLocalDate()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<StatisticsDTO.DailyTrendData> trendData = new java.util.ArrayList<>();

        // Итерируемся по каждому дню в диапазоне, чтобы обеспечить наличие данных даже для дней без запусков
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<TestRun> runsOnDate = runsByDate.getOrDefault(date, List.of());
            long totalOnDate = runsOnDate.size();
            long passedOnDate = runsOnDate.stream().filter(r -> r.getStatus() == TestRun.TestStatus.PASSED).count();
            double passRateOnDate = (totalOnDate > 0) ? (double) passedOnDate / totalOnDate * 100.0 : 0.0;
            trendData.add(new StatisticsDTO.DailyTrendData(date.format(formatter), passRateOnDate, totalOnDate));
        }

        return trendData;
    }

    /**
     * Вычисляет список самых медленных тестов.
     * <p>
     * <b>ВАЖНО:</b> Текущая реализация генерирует фиктивные данные о длительности.
     * Для корректной работы необходимо добавить поле {@code durationMillis}
     * в сущности {@link TestRun} и {@link com.svedentsov.xaiobserverapp.dto.FailureEventDTO},
     * а также заполнять его при обработке событий.
     * </p>
     * Тесты с проваленным статусом получают случайные, но обычно более высокие, длительности,
     * чтобы имитировать более медленное выполнение перед сбоем.
     *
     * @param allRuns Список всех тестовых запусков для анализа.
     * @param limit   Максимальное количество медленных тестов для возврата.
     * @return Список объектов {@link StatisticsDTO.SlowTestDTO}, представляющих самые медленные тесты.
     */
    private List<StatisticsDTO.SlowTestDTO> calculateTopSlowTests(List<TestRun> allRuns, int limit) {
        // --- ВАЖНО: Для этой части требуются фактические данные о длительности теста. ---
        // Для демонстрации мы будем генерировать фиктивные длительности или предполагать, что поле существует.
        // Вы ДОЛЖНЫ добавить 'private Long durationMillis;' в TestRun и FailureEventDTO,
        // и заполнять его при обработке событий.

        // Фиктивная реализация: Присваиваем случайные длительности для демонстрации.
        // В реальном приложении TestRun будет иметь поле `durationMillis`.
        // Отфильтровываем тесты без длительности (если поле существует) или фиктивные.
        return allRuns.stream()
                .map(run -> {
                    // Заглушка для фактической длительности. Здесь нужно использовать `run.getDurationMillis()`.
                    // Сейчас используется фиктивная случайная длительность для пройденных тестов,
                    // и более высокая случайная длительность для проваленных тестов, чтобы имитировать медленные сбои.
                    long dummyDuration = 0;
                    if (run.getStatus() == TestRun.TestStatus.PASSED) {
                        dummyDuration = (long) (Math.random() * 1000 + 500); // 0.5с - 1.5с
                    } else if (run.getStatus() == TestRun.TestStatus.FAILED) {
                        dummyDuration = (long) (Math.random() * 5000 + 2000); // 2с - 7с
                    }
                    return new StatisticsDTO.SlowTestDTO(
                            String.format("%s.%s", run.getTestClass(), run.getTestMethod()),
                            dummyDuration
                    );
                })
                .collect(Collectors.groupingBy(StatisticsDTO.SlowTestDTO::getTestName,
                        Collectors.averagingDouble(StatisticsDTO.SlowTestDTO::getAverageDurationMillis)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> new StatisticsDTO.SlowTestDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
