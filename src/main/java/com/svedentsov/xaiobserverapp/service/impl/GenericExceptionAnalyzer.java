package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.FailureAnalyzer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Универсальный анализатор для необработанных исключений.
 * Должен быть вызван последним в цепочке, если более специфичные анализаторы не сработали.
 */
@Component
@Order(100)
public class GenericExceptionAnalyzer implements FailureAnalyzer {

    @Override
    public boolean canAnalyze(FailureEventDTO event) {
        return event.getExceptionType() != null && !event.getExceptionType().isEmpty();
    }

    @Override
    public AnalysisResult analyze(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Анализ по типу исключения (общее)");
        String exceptionType = event.getExceptionType();
        ar.setAiConfidence(0.50);
        ar.setSuggestedReason("Произошло необработанное исключение: " + exceptionType);
        ar.setSolution("Это исключение не относится к наиболее частым. Проанализируйте полный стек-трейс для определения точной причины. Проверьте логи приложения на сервере на момент выполнения теста.");
        ar.setRawData("Exception Type: " + exceptionType + "\nStack Trace:\n" + event.getStackTrace());
        return ar;
    }
}
