package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.FailureAnalyzer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Анализатор для сбоев, вызванных ошибками утверждений ({@code AssertionError}).
 * <p>
 * Этот тип ошибки указывает на то, что фактический результат выполнения теста
 * не соответствует ожидаемому результату, который был заложен в утверждении (assert).
 * Это может свидетельствовать о функциональном баге в приложении или о некорректной
 * логике самого теста.
 */
@Component
@Order(11)
public class AssertionErrorAnalyzer implements FailureAnalyzer {

    /**
     * Проверяет, является ли тип исключения {@code AssertionError}.
     *
     * @param event DTO события сбоя.
     * @return {@code true}, если тип исключения содержит "AssertionError", иначе {@code false}.
     */
    @Override
    public boolean canAnalyze(FailureEventDTO event) {
        return event.getExceptionType() != null && event.getExceptionType().contains("AssertionError");
    }

    /**
     * Предоставляет анализ, указывающий на несоответствие ожидания и реальности в тесте.
     *
     * @param event DTO события сбоя.
     * @return {@link AnalysisResult} с рекомендацией проверить логику утверждения и фактические данные.
     */
    @Override
    public AnalysisResult analyze(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Анализ по типу исключения");
        ar.setAiConfidence(0.80);
        ar.setSuggestedReason("Сработало утверждение (assertion), что означает несоответствие фактического результата ожидаемому. Это указывает на баг в приложении или ошибку в логике самого теста.");
        ar.setSolution("Проанализируйте, какое именно утверждение не выполнилось. Сравните фактическое и ожидаемое значения. Это может быть как реальный дефект, так и неверно заданные ожидания в тесте.");
        ar.setRawData("Exception Type: " + event.getExceptionType() + "\nStack Trace:\n" + event.getStackTrace());
        return ar;
    }
}
