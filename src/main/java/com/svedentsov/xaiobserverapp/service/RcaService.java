package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.xai.XaiServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Сервис анализа первопричин (Root Cause Analysis - RCA).
 * Оркестрирует процесс анализа сбоев тестов.
 * <p>
 * Принцип работы:
 * 1. Получает на вход событие о завершении теста {@link FailureEventDTO}.
 * 2. Если тест успешен, возвращает стандартное резюме.
 * 3. Если тест провален, последовательно применяет цепочку "rule-based" стратегий анализа
 * ({@link AnalysisStrategy}), отсортированных по приоритету (аннотация @Order).
 * Используется первая стратегия, которая сможет обработать сбой.
 * 4. Если ни одна из "rule-based" стратегий не сработала, сервис обращается
 * к внешнему XAI-сервису (например, ML-модели) для получения предиктивного анализа.
 * 5. Если и XAI-сервис не дал ответа, формируется общее (fallback) сообщение об ошибке.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RcaService {

    private final List<AnalysisStrategy> analysisStrategies;
    private final XaiServiceClient xaiServiceClient;

    /**
     * Выполняет полный анализ события о завершении теста.
     *
     * @param event DTO с данными о тестовом запуске.
     * @return Список результатов анализа.
     */
    public List<AnalysisResult> analyzeTestRun(FailureEventDTO event) {
        List<AnalysisResult> results = new ArrayList<>();

        if ("PASSED".equalsIgnoreCase(event.status())) {
            results.add(createSuccessfulRunSummary());
            return results;
        }

        for (var strategy : analysisStrategies) {
            var resultOpt = strategy.analyze(event);
            if (resultOpt.isPresent()) {
                log.info("Analysis found by rule-based strategy: {}", strategy.getClass().getSimpleName());
                var result = resultOpt.get();
                result.setAnalysisTimestamp(LocalDateTime.now());
                results.add(result);
                return results;
            }
        }

        log.info("No specific rule-based strategy found. Calling XAI service as a fallback...");
        xaiServiceClient.getPrediction(event).ifPresentOrElse(
                mlResult -> {
                    log.info("Received prediction from XAI service.");
                    mlResult.setAnalysisTimestamp(LocalDateTime.now());
                    results.add(mlResult);
                },
                () -> {
                    log.warn("XAI service did not provide a prediction. Falling back to generic analysis.");
                    results.add(createGeneralFailureSummary(event));
                }
        );
        return results;
    }

    /**
     * Создает стандартный результат для успешно пройденного теста.
     *
     * @return {@link AnalysisResult} для успешного запуска.
     */
    private AnalysisResult createSuccessfulRunSummary() {
        var ar = new AnalysisResult();
        ar.setAnalysisType("Резюме успешного запуска");
        ar.setAiConfidence(1.0);
        ar.setSuggestedReason("Тест успешно завершен. Все шаги выполнены корректно.");
        ar.setSolution("Никаких действий не требуется.");
        ar.setAnalysisTimestamp(LocalDateTime.now());
        ar.setExplanationData(Map.of("status", "PASSED"));
        return ar;
    }

    /**
     * Создает общий результат для сбоя, если ни один анализатор не сработал.
     *
     * @param event DTO события.
     * @return {@link AnalysisResult} с общей информацией.
     */
    private AnalysisResult createGeneralFailureSummary(FailureEventDTO event) {
        var ar = new AnalysisResult();
        ar.setAnalysisType("Общий анализ сбоя");
        ar.setAiConfidence(0.40);
        ar.setSuggestedReason("Тест завершился со статусом FAILED, но не удалось определить конкретную причину на основе правил или ответа ML-сервиса.");
        ar.setSolution("Проверьте логи выполнения теста, скриншоты (если они есть) и состояние окружения.");
        ar.setAnalysisTimestamp(LocalDateTime.now());
        ar.setExplanationData(Map.of("status", event.status(), "fallback_reason", "No specific analyzer triggered"));
        return ar;
    }
}