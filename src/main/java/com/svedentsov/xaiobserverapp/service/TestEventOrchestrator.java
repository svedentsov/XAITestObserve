package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.mapper.TestRunMapper;
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

import java.util.concurrent.CompletableFuture;

/**
 * Сервис-оркестратор, управляющий полным циклом обработки события о завершении теста.
 * Этот класс является центральной точкой в архитектуре обработки событий. Его единственная
 * ответственность (SRP) — координировать взаимодействие между другими сервисами
 * (сохранение, анализ, уведомление), не реализуя их логику самостоятельно.
 * Операция выполняется асинхронно ({@code @Async}), чтобы не блокировать вызывающий поток
 * (например, REST-контроллер), обеспечивая высокую отзывчивость API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Validated // Включает проверку @Valid для методов внутри сервиса
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
     * <ol>
     *   <li>Поиск или создание соответствующей {@link TestConfiguration}.</li>
     *   <li>Преобразование DTO в сущность {@link TestRun}.</li>
     *   <li>Запуск анализа причин сбоя через {@link RcaService}.</li>
     *   <li>Сохранение тестового запуска и результатов анализа в БД в одной транзакции.</li>
     *   <li>Отправку уведомления клиентам через WebSocket.</li>
     *   <li>Отправку уведомления о сбое (если применимо).</li>
     *   <li>Сброс кэша статистики для немедленного обновления.</li>
     * </ol>
     *
     * @param event Валидный DTO с данными о тестовом запуске. Аннотация {@code @Valid} запускает валидацию.
     * @return {@link CompletableFuture}, который завершается с сохраненной сущностью {@link TestRun}.
     */
    @Async
    @Transactional
    public CompletableFuture<TestRun> processAndSaveTestEvent(@Valid FailureEventDTO event) {
        log.info("Starting async processing for test run ID: {}", event.testRunId());
        try {
            // 1. Найти или создать уникальную конфигурацию
            var config = testConfigurationService.findOrCreateConfiguration(event);

            // 2. Преобразовать DTO в сущность
            var testRun = testRunMapper.toEntity(event);
            testRun.setConfiguration(config);

            // 3. Провести анализ причин сбоя (RCA)
            var analysisResults = rcaService.analyzeTestRun(event);
            analysisResults.forEach(testRun::addAnalysisResult);

            // 4. Сохранить все в одной транзакции
            var savedTestRun = testRunRepository.save(testRun);
            log.info("Test run with ID {} and its analysis have been successfully saved.", savedTestRun.getId());

            // 5. Отправить уведомление клиентам через WebSocket
            var dto = testRunMapper.toDetailDto(savedTestRun);
            messagingTemplate.convertAndSend("/topic/new-test-run", dto);

            // 6. Отправить уведомление о сбое (если применимо)
            if (savedTestRun.getStatus() == TestRun.TestStatus.FAILED) {
                notificationService.notifyAboutFailure(savedTestRun);
            }

            // 7. Сбросить кэш статистики
            statisticsService.clearStatisticsCache();

            return CompletableFuture.completedFuture(savedTestRun);
        } catch (Exception e) {
            log.error("Failed to process test event for run ID: {}", event.testRunId(), e);
            // Возвращаем проваленный Future для корректной обработки ошибок
            return CompletableFuture.failedFuture(e);
        }
    }
}
