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

    @Async // Вся обработка будет выполняться в отдельном потоке
    @Transactional
    public void processAndSaveTestEvent(@Valid FailureEventDTO event) {
        log.info("Starting processing test event for run ID: {}", event.getTestRunId());
        // 1. Находим или создаем конфигурацию
        TestConfiguration config = testConfigurationService.findOrCreateConfiguration(event);
        // 2. Конвертируем DTO в сущность TestRun
        TestRun testRun = testRunMapper.toEntity(event);
        testRun.setConfiguration(config);
        // 3. Выполняем RCA и связываем результаты.
        List<AnalysisResult> analysisResults = rcaService.analyzeTestRun(event);
        analysisResults.forEach(testRun::addAnalysisResult);
        // 4. Сохраняем TestRun вместе со всеми связанными сущностями
        TestRun savedTestRun = testRunRepository.save(testRun);
        log.info("Test run with ID {} and its analysis results have been saved.", savedTestRun.getId());
        // 5. Отправляем уведомление при необходимости
        if (savedTestRun.getStatus() == TestRun.TestStatus.FAILED) {
            notificationService.notifyAboutFailure(savedTestRun);
        }
    }
}
