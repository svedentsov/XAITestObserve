package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.FailureAnalyzer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Анализатор для сбоев, вызванных {@code TimeoutException}.
 * <p>
 * Это исключение обычно возникает, когда явное ожидание (например, WebDriverWait)
 * не дожидается выполнения условия за отведенное время.
 */
@Component
@Order(13)
public class TimeoutExceptionAnalyzer implements FailureAnalyzer {

    /**
     * Проверяет, является ли тип исключения {@code TimeoutException}.
     *
     * @param event DTO события сбоя.
     * @return {@code true}, если тип исключения содержит "TimeoutException", иначе {@code false}.
     */
    @Override
    public boolean canAnalyze(FailureEventDTO event) {
        return event.getExceptionType() != null && event.getExceptionType().contains("TimeoutException");
    }

    /**
     * Предоставляет анализ и возможные решения для проблем, связанных с таймаутами.
     *
     * @param event DTO события сбоя.
     * @return {@link AnalysisResult} с рекомендациями по увеличению таймаутов или проверке производительности.
     */
    @Override
    public AnalysisResult analyze(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Анализ по типу исключения");
        ar.setAiConfidence(0.85);
        ar.setSuggestedReason("Операция не была завершена за отведенное время. Это может быть связано с медленной загрузкой страницы, медленным ответом от бэкенда или слишком коротким таймаутом в тесте.");
        ar.setSolution("1. Увеличьте время ожидания (timeout) в тесте. 2. Проверьте производительность приложения и сетевые задержки. 3. Оптимизируйте условия ожидания, чтобы они были более гибкими.");
        ar.setRawData("Exception Type: " + event.getExceptionType() + "\nStack Trace:\n" + event.getStackTrace());
        return ar;
    }
}
