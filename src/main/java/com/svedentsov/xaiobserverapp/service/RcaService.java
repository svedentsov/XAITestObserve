package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RcaService {

    private final List<FailureAnalyzer> failureAnalyzers;

    public RcaService(List<FailureAnalyzer> failureAnalyzers) {
        this.failureAnalyzers = failureAnalyzers;
    }

    /**
     * Выполняет анализ тестового запуска и генерирует список {@link AnalysisResult}.
     * Если тест пройден, создается сводка успешного запуска.
     * Если тест провален, итерирует по зарегистрированным {@link FailureAnalyzer}
     * для определения возможных причин и предложений решений.
     *
     * @param event DTO, содержащий информацию о событии завершения теста.
     * @return Список объектов {@link AnalysisResult}, представляющих результаты анализа.
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
     * Создает сводный результат анализа для успешно пройденного тестового запуска.
     *
     * @return Объект {@link AnalysisResult} с информацией об успешном завершении.
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
     * Создает общий сводный результат анализа для проваленного тестового запуска,
     * когда более специфичные причины не могут быть определены.
     *
     * @param event DTO, содержащий информацию о событии сбоя.
     * @return Объект {@link AnalysisResult} с общим анализом сбоя.
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
