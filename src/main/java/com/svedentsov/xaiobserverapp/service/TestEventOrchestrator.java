package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.dto.TestRunDetailDTO;
import com.svedentsov.xaiobserverapp.mapper.TestRunMapper;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.model.TestConfiguration;
import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.repository.TestRunRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис-оркестратор, управляющий полным циклом обработки события о завершении теста.
 * Выполняется асинхронно, чтобы не блокировать вызывающий поток (например, REST-контроллер).
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
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Асинхронно обрабатывает и сохраняет событие о завершении теста.
     * Процесс включает:
     * 1. Преобразование DTO в сущность {@link TestRun}.
     * 2. Поиск или создание соответствующей {@link TestConfiguration}.
     * 3. Запуск анализа причин сбоя через {@link RcaService}.
     * 4. Сохранение тестового запуска и результатов анализа в БД.
     * 5. Отправку уведомления клиентам через WebSocket.
     * 6. Отправку уведомления о сбое (если применимо).
     * 7. Сброс кэша статистики.
     *
     * @param event Валидный DTO с данными о тестовом запуске.
     * @return {@link CompletableFuture}, который завершается с сохраненной сущностью {@link TestRun}.
     */
    @Async
    @Transactional
    public CompletableFuture<TestRun> processAndSaveTestEvent(@Valid FailureEventDTO event) {
        log.info("Starting processing test event for run ID: {}", event.testRunId());
        TestRun testRun = testRunMapper.toEntity(event);
        TestConfiguration config = testConfigurationService.findOrCreateConfiguration(event);
        testRun.setConfiguration(config);
        List<AnalysisResult> analysisResults = rcaService.analyzeTestRun(event);
        analysisResults.forEach(testRun::addAnalysisResult);
        TestRun savedTestRun = testRunRepository.save(testRun);
        TestRunDetailDTO dto = testRunMapper.toDetailDto(savedTestRun);
        messagingTemplate.convertAndSend("/topic/new-test-run", dto);

        log.info("Test run with ID {} and its analysis results have been saved.", savedTestRun.getId());
        if (savedTestRun.getStatus() == TestRun.TestStatus.FAILED) {
            notificationService.notifyAboutFailure(savedTestRun);
        }
        statisticsService.clearStatisticsCache();
        return CompletableFuture.completedFuture(savedTestRun);
    }
}
