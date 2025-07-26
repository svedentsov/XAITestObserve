package com.svedentsov.xaiobserverapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsDTO {
    private long totalRuns;
    private long passedRuns;
    private long failedRuns;
    private long skippedRuns;
    private double passRate;
    private Map<String, Long> failureCountByTest;
    private List<DailyTrendData> dailyPassRateTrend;
    private List<SlowTestDTO> topSlowTests;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyTrendData {
        private String date;
        private double passRate;
        private long totalRuns;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SlowTestDTO {
        private String testName;
        private double averageDurationMillis;
    }
}
