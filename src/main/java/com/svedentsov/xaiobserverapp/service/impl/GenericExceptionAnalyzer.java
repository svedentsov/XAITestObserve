package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.FailureAnalyzer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Общий анализатор исключений, который используется как "запасной" вариант.
 * <p>
 * Этот анализатор имеет самый низкий приоритет и срабатывает только в том случае,
 * если ни один из более специфичных анализаторов не смог обработать событие.
 * Он просто сообщает о типе возникшего исключения.
 */
@Component
@Order(100)
public class GenericExceptionAnalyzer implements FailureAnalyzer {

    /**
     * Проверяет, содержит ли событие какой-либо тип исключения.
     *
     * @param event DTO события сбоя.
     * @return {@code true}, если поле {@code exceptionType} не пустое, иначе {@code false}.
     */
    @Override
    public boolean canAnalyze(FailureEventDTO event) {
        return event.getExceptionType() != null && !event.getExceptionType().isEmpty();
    }

    /**
     * Создает общий результат анализа, основанный на типе исключения.
     *
     * @param event DTO события сбоя.
     * @return {@link AnalysisResult} с общей информацией об исключении.
     */
    @Override
    public AnalysisResult analyze(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Анализ по типу исключения (общее)");
        String exceptionType = event.getExceptionType();
        ar.setAiConfidence(0.50); // Низкая уверенность, так как анализ неглубокий
        ar.setSuggestedReason("Произошло необработанное исключение: " + exceptionType);
        ar.setSolution("Это исключение не относится к наиболее частым. Проанализируйте полный стек-трейс для определения точной причины. Проверьте логи приложения на сервере на момент выполнения теста.");
        ar.setRawData("Exception Type: " + exceptionType + "\nStack Trace:\n" + event.getStackTrace());
        return ar;
    }
}
