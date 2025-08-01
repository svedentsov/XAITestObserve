package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сервис для анализа первопричин (Root Cause Analysis - RCA) сбоев тестов.
 * <p>
 * Использует цепочку анализаторов (реализаций {@link FailureAnalyzer}),
 * чтобы определить наиболее вероятную причину сбоя на основе предоставленных данных.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RcaService {

    private final List<FailureAnalyzer> ruleBasedAnalyzers;
    private final RestTemplate restTemplate;

    // Лучшая практика: вынести URL в application.properties
    @Value("${xai.analysis.service.url:http://localhost:8000/predict}")
    private String mlServiceUrl;

    public List<AnalysisResult> analyzeTestRun(FailureEventDTO event) {
        List<AnalysisResult> results = new ArrayList<>();

        if ("PASSED".equalsIgnoreCase(event.getStatus())) {
            results.add(createSuccessfulRunSummary());
            return results;
        }

        // 1. Попробовать найти причину с помощью rule-based анализаторов
        for (FailureAnalyzer analyzer : ruleBasedAnalyzers) {
            if (analyzer.canAnalyze(event)) {
                log.info("Analyzing with rule-based analyzer: {}", analyzer.getClass().getSimpleName());
                results.add(analyzer.analyze(event));
                // Если нашли, выходим из цикла и не идем к ML
                return results;
            }
        }

        // 2. Если ни одно правило не сработало, обращаемся к ML-сервису
        log.info("No specific rule-based analyzer found. Calling ML analysis service...");
        try {
            AnalysisResult mlResult = restTemplate.postForObject(mlServiceUrl, event, AnalysisResult.class);
            if (mlResult != null && Objects.nonNull(mlResult.getSuggestedReason())) {
                log.info("Received a valid prediction from ML service.");
                results.add(mlResult);
            } else {
                log.warn("ML service returned null or empty result. Falling back to generic analysis.");
                results.add(createGeneralFailureSummary(event));
            }
        } catch (RestClientException e) {
            log.error("ML Analysis service call failed: {}. Falling back to generic analysis.", e.getMessage());
            results.add(createGeneralFailureSummary(event));
        }

        // 3. Если и ML-сервис недоступен, и правила не сработали, возвращаем самый общий анализ
        if (results.isEmpty()) {
            results.add(createGeneralFailureSummary(event));
        }

        return results;
    }

    /**
     * Создает стандартный результат для успешно пройденного теста.
     *
     * @return Результат анализа.
     */
    private AnalysisResult createSuccessfulRunSummary() {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Резюме успешного запуска");
        ar.setAiConfidence(0.99);
        ar.setSuggestedReason("Тест успешно завершен. Все шаги выполнены корректно.");
        ar.setSolution("Никаких действий не требуется.");
        ar.setRawData("Статус: PASSED");
        return ar;
    }

    /**
     * Создает общий результат для сбоя, если ни один конкретный анализатор не сработал.
     *
     * @param event Событие сбоя.
     * @return Результат анализа.
     */
    private AnalysisResult createGeneralFailureSummary(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Общий анализ сбоя");
        ar.setAiConfidence(0.40);
        ar.setSuggestedReason("Тест завершился со статусом FAILED, но не удалось определить конкретную причину на основе предоставленных данных (шаг сбоя или тип исключения).");
        ar.setSolution("Проверьте логи выполнения теста, скриншоты (если они есть) и состояние окружения. Возможно, проблема связана с инфраструктурой или внешними сервисами.");
        ar.setRawData("Статус: " + event.getStatus());
        return ar;
    }
}
