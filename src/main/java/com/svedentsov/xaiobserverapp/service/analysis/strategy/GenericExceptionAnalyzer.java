package com.svedentsov.xaiobserverapp.service.analysis.strategy;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.AnalysisStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * Общая (fallback) стратегия анализа.
 * Срабатывает, если ни одна из более специфичных стратегий не смогла обработать сбой.
 * Этот анализатор просто извлекает тип исключения и предлагает общие рекомендации.
 * Имеет самый низкий приоритет выполнения.
 */
@Component
@Order(100) // Самый низкий приоритет, срабатывает в последнюю очередь
public class GenericExceptionAnalyzer implements AnalysisStrategy {

    @Override
    public Optional<AnalysisResult> analyze(FailureEventDTO event) {
        String exceptionType = event.exceptionType();
        if (StringUtils.hasText(exceptionType)) {
            AnalysisResult ar = new AnalysisResult();
            ar.setAnalysisType("Анализ по типу исключения (общее)");
            ar.setAiConfidence(0.50); // Низкая уверенность, так как анализ неглубокий
            ar.setSuggestedReason("Произошло необработанное исключение: " + exceptionType);
            ar.setSolution("Это исключение не относится к наиболее частым. Проанализируйте полный стек-трейс для определения точной причины. Проверьте логи приложения на сервере на момент выполнения теста.");
            ar.setExplanationData(Map.of("evidence", "Exception Type: " + exceptionType));
            return Optional.of(ar);
        }
        return Optional.empty();
    }
}
