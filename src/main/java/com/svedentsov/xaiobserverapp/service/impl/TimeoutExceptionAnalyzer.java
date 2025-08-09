package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.AnalysisStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * Стратегия анализа, специализирующаяся на {@code org.openqa.selenium.TimeoutException}.
 * Этот анализатор обрабатывает случаи, когда операция ожидания (например, WebDriverWait)
 * не завершилась в установленный лимит времени.
 */
@Component
@Order(18)
public class TimeoutExceptionAnalyzer implements AnalysisStrategy {

    private static final String EXCEPTION_NAME = "TimeoutException";

    @Override
    public Optional<AnalysisResult> analyze(FailureEventDTO event) {
        String exceptionType = event.exceptionType();
        if (StringUtils.hasText(exceptionType) && exceptionType.contains(EXCEPTION_NAME)) {
            AnalysisResult ar = new AnalysisResult();
            ar.setAnalysisType("Анализ по типу исключения (TimeoutException)");
            ar.setAiConfidence(0.85);
            ar.setSuggestedReason("Операция не была завершена за отведенное время. Это может быть связано с медленной загрузкой страницы, медленным ответом от бэкенда или слишком коротким таймаутом в тесте.");
            ar.setSolution("1. Увеличьте время ожидания (timeout) в тесте. 2. Проверьте производительность приложения и сетевые задержки. 3. Оптимизируйте условия ожидания, чтобы они были более гибкими.");
            ar.setExplanationData(Map.of("evidence", "Exception Type: " + event.exceptionType()));
            return Optional.of(ar);
        }
        return Optional.empty();
    }
}
