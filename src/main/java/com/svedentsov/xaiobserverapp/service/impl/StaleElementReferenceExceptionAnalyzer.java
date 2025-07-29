package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.FailureAnalyzer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Анализатор для сбоев, вызванных {@code StaleElementReferenceException}.
 * <p>
 * Это исключение возникает, когда DOM-структура страницы изменяется после того,
 * как элемент был найден, но до того, как с ним произошло взаимодействие,
 * делая ссылку на элемент недействительной.
 */
@Component
@Order(10)
public class StaleElementReferenceExceptionAnalyzer implements FailureAnalyzer {

    /**
     * Проверяет, является ли тип исключения {@code StaleElementReferenceException}.
     *
     * @param event DTO события сбоя.
     * @return {@code true}, если тип исключения содержит "StaleElementReferenceException", иначе {@code false}.
     */
    @Override
    public boolean canAnalyze(FailureEventDTO event) {
        return event.getExceptionType() != null && event.getExceptionType().contains("StaleElementReferenceException");
    }

    /**
     * Предоставляет анализ и стандартное решение для {@code StaleElementReferenceException}.
     *
     * @param event DTO события сбоя.
     * @return {@link AnalysisResult} с рекомендациями по исправлению данной ошибки.
     */
    @Override
    public AnalysisResult analyze(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Анализ по типу исключения");
        ar.setAiConfidence(0.95); // Высокая уверенность, так как причина обычно однозначна
        ar.setSuggestedReason("Элемент, с которым пытались взаимодействовать, устарел. Это происходит, когда DOM-структура страницы динамически изменяется (например, через AJAX), и ссылка на элемент становится недействительной.");
        ar.setSolution("Не сохраняйте WebElement в переменную для долгого использования. Вместо этого, находите элемент заново непосредственно перед каждым взаимодействием. Используйте паттерн Page Object Model для инкапсуляции логики поиска элементов.");
        ar.setRawData("Exception Type: " + event.getExceptionType() + "\nStack Trace:\n" + event.getStackTrace());
        return ar;
    }
}
