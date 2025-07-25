package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.StatisticsDTO;
import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.repository.TestRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final TestRunRepository testRunRepository;

    @Transactional(readOnly = true)
    @Cacheable("statistics")
    public StatisticsDTO getOverallStatistics() {
        List<TestRun> allRuns = testRunRepository.findAll();
        long total = allRuns.size();
        if (total == 0) {
            return new StatisticsDTO(0, 0, 0, 0, 0.0, Map.of(), List.of(), List.of());
        }

        long passed = allRuns.stream().filter(r -> r.getStatus() == TestRun.TestStatus.PASSED).count();
        long failed = allRuns.stream().filter(r -> r.getStatus() == TestRun.TestStatus.FAILED).count();
        long skipped = total - passed - failed;
        double passRate = (total > 0) ? (double) passed / total * 100.0 : 0.0;

        Map<String, Long> failingTests = testRunRepository.findTopFailingTests().stream()
                .limit(10)
                .collect(Collectors.toMap(
                        record -> (String) record[0],
                        record -> (Long) record[1],
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
        List<StatisticsDTO.DailyTrendData> dailyPassRateTrend = calculateDailyPassRateTrend(allRuns, 30);
        List<StatisticsDTO.SlowTestDTO> topSlowTests = calculateTopSlowTests(allRuns, 5);
        return new StatisticsDTO(total, passed, failed, skipped, passRate, failingTests, dailyPassRateTrend, topSlowTests);
    }

    private List<StatisticsDTO.DailyTrendData> calculateDailyPassRateTrend(List<TestRun> allRuns, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        Map<LocalDate, List<TestRun>> runsByDate = allRuns.stream()
                .filter(run -> run.getTimestamp() != null &&
                        run.getTimestamp().toLocalDate().isAfter(startDate.minusDays(1)) &&
                        run.getTimestamp().toLocalDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.groupingBy(run -> run.getTimestamp().toLocalDate()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<StatisticsDTO.DailyTrendData> trendData = new java.util.ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<TestRun> runsOnDate = runsByDate.getOrDefault(date, List.of());
            long totalOnDate = runsOnDate.size();
            long passedOnDate = runsOnDate.stream().filter(r -> r.getStatus() == TestRun.TestStatus.PASSED).count();
            double passRateOnDate = (totalOnDate > 0) ? (double) passedOnDate / totalOnDate * 100.0 : 0.0;
            trendData.add(new StatisticsDTO.DailyTrendData(date.format(formatter), passRateOnDate, totalOnDate));
        }

        return trendData;
    }

    private List<StatisticsDTO.SlowTestDTO> calculateTopSlowTests(List<TestRun> allRuns, int limit) {
        return allRuns.stream()
                .map(run -> {
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
