package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.mapper.TestRunMapper;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.model.TestConfiguration;
import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.repository.TestRunRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис-оркестратор для обработки событий о завершении тестов.
 * <p>
 * Координирует процесс сохранения данных о тесте, вызов анализа причин сбоя,
 * отправку уведомлений и очистку кэшей. Все операции выполняются асинхронно.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class TestEventOrchestrator {

    private final TestRunRepository testRunRepository;
    private final TestConfigurationService testConfigurationService;
    private final RcaService rcaService;
    private final NotificationService notificationService;
    private final TestRunMapper testRunMapper;
    private final StatisticsService statisticsService;

    /**
     * Асинхронно обрабатывает и сохраняет событие о завершении теста.
     * <p>
     * Выполняет следующие шаги:
     * 1. Находит или создает конфигурацию теста.
     * 2. Преобразует DTO в сущность.
     * 3. Запускает анализ причин сбоя.
     * 4. Сохраняет сущность и результаты анализа.
     * 5. Отправляет уведомление в случае сбоя.
     * 6. Очищает кэш статистики.
     *
     * @param event Валидный DTO {@link FailureEventDTO} с данными о тесте.
     * @return {@link CompletableFuture} с сохраненной сущностью {@link TestRun}.
     */
    @Async
    @Transactional
    public CompletableFuture<TestRun> processAndSaveTestEvent(@Valid FailureEventDTO event) {
        log.info("Starting processing test event for run ID: {}", event.getTestRunId());
        TestConfiguration config = testConfigurationService.findOrCreateConfiguration(event);
        TestRun testRun = testRunMapper.toEntity(event);
        testRun.setConfiguration(config);
        testRun.setEnvironmentDetails(event.getEnvironmentDetails());
        testRun.setArtifacts(event.getArtifacts());
        testRun.setCustomMetadata(event.getCustomMetadata());
        testRun.setTestTags(event.getTestTags());
        List<AnalysisResult> analysisResults = rcaService.analyzeTestRun(event);
        analysisResults.forEach(testRun::addAnalysisResult);
        TestRun savedTestRun = testRunRepository.save(testRun);
        log.info("Test run with ID {} and its analysis results have been saved.", savedTestRun.getId());
        if (savedTestRun.getStatus() == TestRun.TestStatus.FAILED) {
            notificationService.notifyAboutFailure(savedTestRun);
        }
        statisticsService.clearStatisticsCache();
        return CompletableFuture.completedFuture(savedTestRun);
    }
}
