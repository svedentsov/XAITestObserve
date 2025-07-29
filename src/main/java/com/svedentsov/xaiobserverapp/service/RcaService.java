package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для анализа первопричин (Root Cause Analysis - RCA) сбоев тестов.
 * <p>
 * Использует цепочку анализаторов (реализаций {@link FailureAnalyzer}),
 * чтобы определить наиболее вероятную причину сбоя на основе предоставленных данных.
 */
@Service
public class RcaService {

    private final List<FailureAnalyzer> failureAnalyzers;

    /**
     * Конструктор, который автоматически внедряет все бины, реализующие интерфейс {@link FailureAnalyzer}.
     * Spring автоматически сортирует их на основе аннотации {@code @Order}.
     *
     * @param failureAnalyzers Список анализаторов сбоев.
     */
    public RcaService(List<FailureAnalyzer> failureAnalyzers) {
        this.failureAnalyzers = failureAnalyzers;
    }

    /**
     * Анализирует событие завершения теста для определения причины.
     *
     * @param event DTO события завершения теста.
     * @return Список результатов анализа. Обычно содержит один результат.
     */
    public List<AnalysisResult> analyzeTestRun(FailureEventDTO event) {
        List<AnalysisResult> results = new ArrayList<>();
        if ("PASSED".equalsIgnoreCase(event.getStatus())) {
            results.add(createSuccessfulRunSummary());
            return results;
        }
        for (FailureAnalyzer analyzer : failureAnalyzers) {
            if (analyzer.canAnalyze(event)) {
                results.add(analyzer.analyze(event));
                break;
            }
        }
        if (results.isEmpty() && "FAILED".equalsIgnoreCase(event.getStatus())) {
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
